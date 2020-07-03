const bodyParser = require('body-parser');
const cors = require('cors');
const express = require('express');
const request = require('request');


function createUrlForGetSingleItem(query) {
    return "http://open.api.ebay.com/shopping?callname=GetSingleItem" +
            "&responseencoding=JSON&appid=AnNguyen-SearchIt-PRD-e2eae7200-e11e5648" + 
            "&siteid=0&version=967" + 
            "&ItemID=" + query["itemId"] + 
            "&IncludeSelector=Description,Details,ItemSpecifics";
}


function createUrlForFindItemsAdvanced(query) {
    let url = "https://svcs.ebay.com/services/search/FindingService/v1" +
        "?SECURITY-APPNAME=AnNguyen-SearchIt-PRD-e2eae7200-e11e5648" +
        "&OPERATION-NAME=findItemsAdvanced" +
        "&SERVICE-VERSION=1.0.0" +
        "&RESPONSE-DATA-FORMAT=JSON" +
        "&REST-PAYLOAD" +
        "&keywords=" + query.keywords +
        "&sortOrder=" + query.sortOrder;
    counter = 0;
    url = addOptionalFilters(url, query, counter)
    return url;
}

function addOptionalFilters(url, query, counter) {
    [url, counter] = addPriceFilter(url, query, "MinPrice", counter);
    [url, counter] = addPriceFilter(url, query, "MaxPrice", counter);
    [url, counter] = addFilter(url, query, "ReturnsAcceptedOnly", "true", counter);
    [url, counter] = addFilter(url, query, "FreeShippingOnly", "true", counter);
    [url, counter] = addFilter(url, query, "ExpeditedShippingType", "Expedited", counter);
    url = addConditionFilter(url, query["Condition"], counter);
    return url;
}

function addPriceFilter(url, query, priceType, counter) {
    if (priceType in query) {
        url += "&itemFilter(" + counter + ").name=" + priceType + "&itemFilter(" +
            + counter + ").value=" + query[priceType] + "&itemFilter(" +
            + counter + ").paramName=Currency&itemFilter(" +
            counter + ").paramValue=USD";
        counter += 1;
    };
    return [url, counter];
}

function addFilter(url, query, filteredName, comparedValue, counter) {
    if (query[filteredName] == comparedValue) {
        url += "&itemFilter(" + counter + ").name=" + filteredName + "&itemFilter(" +
            + counter + ").value=" + query[filteredName]
        counter += 1;
    };
    return [url, counter];
}

function addConditionFilter(url, condition, counter) {
    if (condition) {
        url += "&itemFilter(" + counter + ").name=" + "Condition";
        let conditionList = condition.split(',');
        for (let i in conditionList)
            url += "&itemFilter(" + counter + ").value(" + i + ")=" + conditionList[i];
    };
    return url;
}

function extractInfo(json) {
    if ("findItemsAdvancedResponse" in json) {
        let response = json["findItemsAdvancedResponse"][0];
        if ("errorMessage" in response)
            return [];
        let itemCount = response["paginationOutput"][0]["totalEntries"][0];
        if (itemCount == 0)
            return [];
        let items = response["searchResult"][0]["item"];
        let itemList = [];
        for (let item of items) {
			if (itemList.length == 50)
				break;
			getItemInfo(itemList, item);
		}    
        return itemList;
    } else return json;
}

function getItemInfo(itemList, item) {
    if (!("condition" in item && "conditionDisplayName" in item["condition"][0]))
        return;
    if (!("shippingInfo" in item) || !("shippingServiceCost" in item["shippingInfo"][0]))
        return;
    itemList.push(item);
}

let PORT = 8080;
const app = express();
app.use(bodyParser.json());
app.use(cors());

app.get('/', function (req, res) {
    res.send('Hey, you are not supposed to be here! Go to https://nodejs-example-1621.appspot.com');
})

app.get('/search', function (req, res) {
    console.log(req.originalUrl);
    let query = req.query;
    let url = "";
    if ("itemId" in query)
        url = createUrlForGetSingleItem(query);
    else
        url = createUrlForFindItemsAdvanced(query); 
	// console.log(url);
    request(url, { json: true }, (err, _, body) => {
        if (err) {
            res.send({ 'message': 'Something\'s wrong!' });
            throw err;
        } else
            res.json(extractInfo(body));
    });
})

app.listen(PORT, function () {
    console.log('Server is running on http://localhost:' + PORT);
})
