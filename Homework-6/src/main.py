from flask import Flask, jsonify, request
import re
import requests

app = Flask(__name__)


@app.route("/", methods=["GET", "POST"])
def index():
    """
    Serve the index page
    :return: None
    """
    return app.send_static_file("index.html")


@app.route("/search", methods=["GET"])
def search():
    """
    Receive the request from the front-end when the user hit the Search button. Then extract the filters from the
    request and use them to form a search url. Use the url to send to a request to Ebay API to receive the data. Extract
    the data, jsonify it, and send it back to the front-end
    :return: a modified version of the json response received from the Ebay API
    """
    print(request)
    filters = get_filters()
    url = generate_url(filters)
    response = requests.get(url).json()
    json_data = extract_information(response)
    return jsonify(json_data)


def get_filters():
    """
    Extract the filtering options sent by the front-end
    :return: a tuple of the filtering options
    """
    keyword = request.args.get("keyword", None)
    sort_order = request.args.get("sortOrder", "BestMatch")
    min_price = float(request.args.get("MinPrice", 0))
    max_price = float(request.args.get("MaxPrice", 0))
    return_accepted = request.args.get("ReturnsAcceptedOnly", "false")
    free_shipping = request.args.get("FreeShippingOnly", "false")
    expedited_shipping = request.args.get("ExpeditedShippingType", None)
    conditions = request.args.get("conditions", None)
    return keyword, sort_order, min_price, max_price, return_accepted, free_shipping, expedited_shipping, conditions


def generate_url(filters):
    """
    Generate the searching url based on the filters extracted. There are 2 mandatory filtering options must be added:
    keywords and sortOrder. There are also 6 optional filtering options that might or might not be specified: min_price,
    max_price, return_accepted (indicating whether the seller accepts returns or not), free_shipping (indicating whether
    the seller includes free shipping with the item), expedited_shipping (indicating whether the seller is willing to
    use Expedited Shipping), and conditions (the conditions of the item: New, Used, Very Good, Good, Acceptable)
    :param filters: a tuple of the filtering options
    :return: the url containing all the filtering options
    """
    url = "https://svcs.ebay.com/services/search/FindingService/v1" \
          "?SECURITY-APPNAME=AnNguyen-SearchIt-PRD-e2eae7200-e11e5648" \
          "&OPERATION-NAME=findItemsAdvanced" \
          "&SERVICE-VERSION=1.0.0" \
          "&RESPONSE-DATA-FORMAT=JSON" \
          "&REST-PAYLOAD" \
          "&keywords=" + filters[0] + \
          "&sortOrder=" + filters[1]
    url = add_optional_filters(url, filters[2:])
    return url


def add_optional_filters(url, optional_filters):
    """
    Add optional filters to the url
    :param url: a url containing all the filtering options
    :param optional_filters: a tuple containing all of the optional filtering options
    :return: a fully-added url containing all the filtering options
    """
    counter = 0
    url, counter = add_price_filter(url, counter, "MinPrice", optional_filters[0])
    url, counter = add_price_filter(url, counter, "MaxPrice", optional_filters[1])
    url, counter = add_filter(url, counter, "ReturnsAcceptedOnly", optional_filters[2], "true")
    url, counter = add_filter(url, counter, "FreeShippingOnly", optional_filters[3], "true")
    url, counter = add_filter(url, counter, "ExpeditedShippingType", optional_filters[4], "Expedited")
    url = add_item_conditions_filter(url, counter, optional_filters[5])
    return url


def add_price_filter(url, counter, price_filter_name, price_value):
    """
    Add price filter (minimum price and maximum price) to the url if they are larger than 0. Otherwise, keep the url as
    is
    :param url: a url containing all the filtering options
    :param counter: number of filter already existed in url
    :param price_filter_name: name of the price filter (I don't know wha to say)
    :param price_value: the value of the price filter
    :return: the url after adding the price filters and the counter
    """
    if price_value > 0:
        url += "&itemFilter(" + str(counter) + ").name=" + price_filter_name + "&itemFilter(" \
               + str(counter) + ").value=" + str(price_value) + "&itemFilter(" \
               + str(counter) + ").paramName=Currency&itemFilter(" + \
            str(counter) + ").paramValue=USD"
        counter += 1
    return url, counter


def add_filter(url, counter, filter_name, filter_value, compared_value):
    if filter_value == compared_value:
        url += "&itemFilter(" + str(counter) + ").name=" + filter_name + "&itemFilter(" \
               + str(counter) + ").value=" + filter_value
        counter += 1
    return url, counter


def add_item_conditions_filter(url, counter, conditions):
    if conditions is not None:
        conditions = conditions.split(",")
        url += "&itemFilter(" + str(counter) + ").name=" + "Condition"
        for index in range(len(conditions)):
            url += "&itemFilter(" + str(counter) + ").value(" + str(index) + ")=" + conditions[index]
    return url


def extract_information(response):
    """
    Extract required information from the response received from the Ebay API.
    :param response: the response received from the Ebay API containing the information of the items matching the
    description
    :return: a dictionary with 2 keys: a number of items that matches the description, and "items" (containing a list of
    no more than 10 items found in the response)
    """
    data = response["findItemsAdvancedResponse"][0]

    # If there is something wrong with the response (ie. Network Error, error with the Ebay server or the Universe
    # itself), return no result
    if "paginationOutput" not in data:
        return {"itemCount": 0, "items": []}
    item_count = int(data["paginationOutput"][0]["totalEntries"][0])

    # Return no result if the Ebay API couldn't find any item matching the description
    if item_count == 0:
        return {"itemCount": 0, "items": []}

    item_list = data["searchResult"][0]["item"]
    items = get_items(item_list)

    # If all of the search results are missing required information, assuming no result is found
    if len(items) == 0:
        return {"itemCount": 0, "items": []}

    if len(items) == 10:
        return {"itemCount": item_count, "items": items}
    return {"itemCount": len(items), "items": items}


def get_items(item_list):
    """
    Get the list with at most 10 items with their information extracted from the Ebay API response
    :param item_list: a list of items with their information sent from the Ebay API
    :return: a list of items (with every entry is a dictionary of an individual item) with maximum length of 10
    """
    items = []
    for item in item_list:
        item_info = {}
        # GETTING MANDATORY ITEM
        is_valid = extract_mandatory_information(item, item_info)
        if not is_valid:
            continue
        # Getting optional information
        is_valid = extract_optional_information(item, item_info)
        if not is_valid:
            continue
        items.append(item_info)
        if len(items) == 10:
            break
    return items


def extract_mandatory_information(item, item_info):
    """
    Extract all of the item's mandatory information, which are the information that will be shown immediately for the
    user after they hit Search
    :param item: a json object returned from Ebay API containing the item's information
    :param item_info: a dict object containing the item information
    :return: False if any of the information, except for the image url, is missing. Otherwise, True
    """
    if "title" not in item:
        return False
    item_info["title"] = item["title"][0]

    if "primaryCategory" not in item or "categoryName" not in item["primaryCategory"][0]:
        return False
    item_info["category"] = item["primaryCategory"][0]["categoryName"][0]

    if "sellingStatus" not in item or "convertedCurrentPrice" not in item["sellingStatus"][0]:
        return False
    item_info["price"] = float(
        item["sellingStatus"][0]["convertedCurrentPrice"][0]["__value__"])

    if "topRatedListing" not in item:
        return False
    item_info["topRatedListing"] = item["topRatedListing"][0]

    if "viewItemURL" not in item:
        return False
    item_info["url"] = item["viewItemURL"][0]

    image_url = ""
    if "galleryURL" in item:
        image_url = item["galleryURL"][0]
    start_pattern = re.compile("^https://thumbs1.ebaystatic.com/pict/")
    end_pattern = re.compile("4040_0.jpg$")
    if not image_url or (start_pattern.search(image_url) and end_pattern.search(image_url)):
        image_url = "static/img/ebay_default.jpg"
    item_info["image"] = image_url

    if "condition" not in item:
        return False
    item_info["condition"] = item["condition"][0]["conditionDisplayName"][0]

    return True


def extract_optional_information(item, item_info):
    """
    Extract all of the item's optional information, which are the information that will be shown when the user expands
    the item's display card
    :param item: a json object returned from Ebay API containing the item's information
    :param item_info: a dict object containing the item information
    :return: False if any of the information is missing. Otherwise, True
    """
    if "location" not in item:
        return False
    item_info["location"] = item["location"][0]

    if "returnsAccepted" not in item:
        return False
    item_info["returnsAccepted"] = item["returnsAccepted"][0]

    if "shippingInfo" not in item:
        return False
    shipping_info = item["shippingInfo"][0]
    if "shippingServiceCost" not in shipping_info:
        return False
    item_info["shippingFee"] = float(
        shipping_info["shippingServiceCost"][0]["__value__"])
    if "expeditedShipping" not in shipping_info:
        return False
    item_info["expeditedShipping"] = shipping_info["expeditedShipping"][0]

    return True


if __name__ == "__main__":
    app.run(debug=True)
