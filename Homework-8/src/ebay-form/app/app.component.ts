import { Component, ElementRef, ViewChild, ViewEncapsulation } from '@angular/core';
import { SearchForm } from './search-form';
import { SearchService } from './search.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class AppComponent {
  // Variables meant to use in the form
  sortOrder = ['Best Match', 'Highest Price', 'Highest Price + Shipping', 'Lowest Price + Shipping'];

  // Variables to generate query
  form = new SearchForm('',
    { minPrice: '', maxPrice: '' },
    { new: false, used: false, vgood: false, good: false, acceptable: false },
    false, false, false, 'BestMatch');
  keyword = '';

  // Variables using for ngIf 
  keywordCondition: boolean = false;
  priceCondition: boolean = false;
  formSubmitted: boolean = false;
  noResultCondition: boolean = true;

  // Variables to store and display result
  items: Array<any>;
  itemCount: number;
  page: number = 1;
  color: Object = {"applicable": "green", "notApplicable": "red"};
  markSize: string = '30px';
  properties = ['expeditedShipping', 'onedayShipping', 'bestOfferEnabled', 'buyItNowAvailable', 'Gift'];

  // Variable to store which index card is currently showing more details
  shownIndices: Array<number> = [];

  constructor(private _searchService: SearchService) { }

  reset() {
    console.log("Clear button is clicked!");
    this.page = 1;
    this.shownIndices = [];
    this.keywordCondition = false;
    this.priceCondition = false;
    this.formSubmitted = false;
    this.noResultCondition = true;
  }

  submit() {
    console.log("Submit button is clicked!");
    this.reset();
    this.keyword = this.form.keywords;
    let minPrice = Number(this.form.price.minPrice);
    let maxPrice = Number(this.form.price.maxPrice);
    this.keywordCondition = this.keyword == null || this.keyword == '';
    this.priceCondition = isNaN(minPrice) || isNaN(maxPrice) || minPrice < 0 || maxPrice < 0 || (maxPrice > 0 && minPrice > maxPrice);
    if (!this.keywordCondition && !this.priceCondition) {
      this._searchService.search(this.form).subscribe(
        data => this.displayData(data),
        error => console.log('Error!', error))
    }
  }

  displayData(data) {
    console.log('Success!', data);
    this.formSubmitted = true;
    this.itemCount = Object.keys(data).length;
    this.noResultCondition = this.itemCount == 0;
    if (this.noResultCondition)
      return;

    this.items = data;
    for (let item of data) {
      for (let p of this.properties) {
        if (p in item && item[p] == 'true') {
          item[p] = { symbol: '<span class="material-icons">done</span>', color: this.color["applicable"], markSize: this.markSize }
        } else {
          item[p] = { symbol: '<span class="material-icons">clear</span>', color: this.color["notApplicable"], markSize: this.markSize }
        }
      }
    }
    
  }

  changePage(event: any) {
    this.shownIndices = [];
  }

  showDetails(event: any, value: number) {
    if (event.target.innerText == "Show Details") {
      this.shownIndices.push(value);
      event.target.innerText = "Hide Details";
    }
    else {
      this.shownIndices.forEach((item, idx) => {
        if (item == value) this.shownIndices.splice(idx, 1);
      });
      event.target.innerText = "Show Details";
    }
  }
}
