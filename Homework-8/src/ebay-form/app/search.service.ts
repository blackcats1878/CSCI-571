import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SearchForm } from './search-form';


@Injectable({
  providedIn: 'root'
})
export class SearchService {

  conditionPairs = { new: 1000, used: 3000, vgood: 4000, good: 5000, acceptable: 6000 };
  sortOptions = {
    'Best Match': 'BestMatch', 'Highest Price': 'CurrentPriceHighest',
    'Highest Price + Shipping': 'PricePlusShippingHighest',
    'Lowest Price + Shipping': 'PricePlusShippingLowest'
  };

  _url = 'http://localhost:8080/search';
  constructor(private _http: HttpClient) { }

  search(form: SearchForm) {
    console.clear();
    let params = this.createParams(form);
    return this._http.get<any>(this._url, { params: params });
  }

  createParams(form: SearchForm) {
    let params = {};
    params['keywords'] = form.keywords;
    params['sortOrder'] = this.sortOptions[form.sortOrder];
    this.addFilter(params, parseFloat(form.price.minPrice) > 0, 'MinPrice', parseFloat(form.price.minPrice));
    this.addFilter(params, parseFloat(form.price.maxPrice) > 0, 'MaxPrice', parseFloat(form.price.maxPrice));
    this.addFilter(params, form.returnAccepted, 'ReturnsAcceptedOnly', form.returnAccepted);
    this.addFilter(params, form.freeShipping, 'FreeShippingOnly', form.freeShipping);
    this.addFilter(params, form.expeditedShipping, 'ExpeditedShippingType', 'Expedited');
    let itemConditions = '';
    for (let key in form.conditions) {
      if (form.conditions[key] == true) {
        itemConditions += this.conditionPairs[key] + ',';
      }
    }
    this.addFilter(params, itemConditions.length > 0, 'Condition', itemConditions.slice(0, itemConditions.length - 1));
    return params;
  }

  addFilter(params: {}, condition: boolean, key: string, value: any) {
    if (condition) {
      params[key] = value;
    }
  }
}
