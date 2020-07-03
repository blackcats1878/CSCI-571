function submit(e) {
    console.clear();
    e.preventDefault();
    var keyword = document.getElementById("keyword").value;
    var sortOrder = getSortOrder();
    var minPrice = Number(document.getElementById("priceFrom").value);
    var maxPrice = Number(document.getElementById("priceTo").value);
    var itemCondition = getItemConditions();
    var returnsAccepted = document.getElementById("return").checked;
    var freeShipping = document.getElementById("ship-free").checked;
    var expeditedShipping = document.getElementById("ship-expedited").checked;
    if (!validatePrice(minPrice, maxPrice)) {
        return;
    }
    var query = createQuery(keyword, sortOrder, minPrice, maxPrice, freeShipping, expeditedShipping,
        returnsAccepted, itemCondition);
    runQuery(query, keyword);
}

function getSortOrder() {
    var sortOptions = document.getElementById("sort")
    return sortOptions.options[sortOptions.selectedIndex].value;
}

function getItemConditions() {
    var itemCondition = [];
    for (var box of document.getElementsByName("conditions")) {
        if (box.checked) {
            itemCondition.push(box.value);
        }
    }
    return itemCondition;
}

function validatePrice(minPrice, maxPrice) {
    if (minPrice < 0 || maxPrice < 0) {
        alert("Price Range value cannot be negative! Please try a value greater than or equal to 0.0.");
        return false;
    }
    if (maxPrice > 0 && minPrice > maxPrice) {
        alert("Lower price limit cannot be grater than upper price limit! Please try again!");
        return false;
    }
    return true;
}

function createQuery(keyword, sortOrder, minPrice, maxPrice, freeShipping, expeditedShipping, returnsAccepted, itemCondition) {
    var query = "?keyword=" + keyword;
    query += "&sortOrder=" + sortOrder;
    query = addFilter(query, minPrice > 0, "MinPrice", minPrice);
    query = addFilter(query, maxPrice > 0, "MaxPrice", maxPrice);
    query = addFilter(query, freeShipping, "FreeShippingOnly", freeShipping);
    query = addFilter(query, expeditedShipping, "ExpeditedShippingType", "Expedited");
    query = addFilter(query, returnsAccepted, "ReturnsAcceptedOnly", returnsAccepted);
    query = addFilter(query, itemCondition.length > 0, "conditions", itemCondition);
    return query
}

function addFilter(query, condition, filter_key, filter_value) {
    if (condition) {
        query += "&" + filter_key + "=" + filter_value
    }
    return query;
}

function runQuery(query, keyword) {
    var url = "/search" + query;
    fetch(url)
        .then(response => response.json())
        .then(data => {
            displayResult(data, keyword);
        });

    // How to achieve the same thing with XMLHttpRequest
    // var xmlhttp = new XMLHttpRequest()
    // xmlhttp.onreadystatechange = function () {
    //     if (this.readyState == 4 && this.status == 200) {
    //         var data = JSON.parse(xmlhttp.responseText);
    //         displayResult(data, keyword);
    //     }
    // }
    // xmlhttp.open("GET", url, true);
    // xmlhttp.send();
}

function displayResult(data, keyword) {
    resetDisplaySection();
    if (data.itemCount > 0) {
        text = data.itemCount + ' Results found for <span style="font-style: italic;">' + keyword + '</span>';
        document.getElementById("line-break").style.visibility = "visible";
        createDisplaySection(data, data.itemCount);
    } else {
        text = "No Results Found";
    }
    document.getElementById("number-of-results").innerHTML = text;
}

function resetDisplaySection() {
    document.getElementById("number-of-results").innerHTML = "";
    document.getElementById("line-break").style.visibility = "hidden";
    document.getElementById("display-section").innerHTML = "";
    document.getElementById("show-more").style.visibility = "hidden";
    document.getElementById("show-more").value = "Show More";
}

function createDisplaySection(data, numberOfResults, itemCounter) {
    var items = data.items;
    displayItems(items);
    if (numberOfResults > 3) {
        document.getElementById("show-more").style.visibility = "visible";
    }
}

function displayItems(items) {
    var itemCounter = 0;
    var cards = "";
    for (var item of items) {
        if (itemCounter == 10) {
            break;
        }
        if (itemCounter >= 3) {
            cards += createDisplayCard(item, " hidden");
        } else {
            cards += createDisplayCard(item, "");
        }
        itemCounter += 1;
    }
    document.getElementById("display-section").innerHTML = cards;
}

function createDisplayCard(item, hiddenClass) {
    var card = "";
    card =
        '<div class="display-card' + hiddenClass + '" onclick="displayDetail.call(this)">' +
        '<div class="display-card-item display-card-image"><img src="' + item.image + '"></div>' +
        '<div class="display-card-item display-card-text">' +
        '<button class="close-button" onclick="closeDisplayCard(this.parentElement.parentElement)">&times</button>' +
        '<p class="child">' +
        '<a href="' + item.url + '" target="_blank">' + item.title + '</a>' +
        '</p>' +
        '<p class="child">' +
        'Category: ' + item.category +
        '<a href="' + item.url + '" target="_blank">' +
        '<img class="redirect" src="static/img/redirect.png">' +
        '</a>' +
        '</p>' +
        '<p class="child">Condition: ' + item.condition;
    if (item.topRatedListing == "true") {
        card += '<span><img class="top-rated" src="static/img/top_rated.png"</span>'
    }
    card += '</p>';

    if (item.returnsAccepted == "true") {
        card += '<p class="child hidden">Seller <span style="font-weight: 900;">accepts</span> returns</p>';
    } else {
        card += '<p class="child hidden">Seller <span style="font-weight: 900;">does not accept</span> returns</p>';
    }

    card += '<p class="child hidden">';
    if (item.shippingFee == 0) {
        card += 'Free Shipping';
    } else {
        card += 'No Free Shipping';
    }
    if (item.expeditedShipping == "true") {
        card += ' -- Expedited Shipping available';
    }
    card += '</p>';

    card +=
        '<p class="child" id="display-price">Price: $' + item.price;
    if (item.shippingFee > 0) {
        card += '( + $' + item.shippingFee + ' for shipping)';
    }

    card += '<span class="hidden" style="font-style: italic; font-weight: 300;">' +
        ' From ' + item.location +
        '</span>' +
        '</p>' +
        '</div>' +
        '</div>';
    return card;
}

var displayDetail = function () {
    changeDisplayCard(this, "300px", "visible", "normal", "inline");
}

function closeDisplayCard(parent) {
    changeDisplayCard(parent, "250px", "hidden", "nowrap", "none");
}

function changeDisplayCard(parent, newHeight, newVisibility, newWhiteSpace, newDisplay) {
    children = parent.children[1].children;
    parent.style.height = newHeight;
    children[0].style.visibility = newVisibility;
    for (var child of children) {
        child.style.whiteSpace = newWhiteSpace;
        if (child.classList[1] == "hidden") {
            child.style.display = newDisplay;
        }
        if (child.children.length > 0) {
            for (var grandchild of child.children) {
                if (grandchild.classList[0] == "hidden") {
                    grandchild.style.display = newDisplay;
                }
            }
        }
    }
    event.stopPropagation();
}

function clickShowMore(e) {
    if (this.value == "Show More") {
        changeDisplay(this, "Show Less", "grid", "show-more");
    } else {
        changeDisplay(this, "Show More", "none", "header-section");
    }
}

function changeDisplay(button, buttonNewValue, display, elementID) {
    button.value = buttonNewValue;
    for (var child of document.getElementById("display-section").children) {
        if (child.classList[1] == "hidden") {
            child.style.display = display;
        }
    }
    scroll(elementID);
}

function scroll(elementID) {
    var element = document.getElementById(elementID);
    element.scrollIntoView({ behavior: "smooth" });
}
