export class SearchForm {
    constructor(
        public keywords: string,
        public price: {minPrice: string, maxPrice: string},
        public conditions: {new: false, used: false, vgood: false, good: false, acceptable: false},
        public returnAccepted: boolean,
        public freeShipping: boolean,
        public expeditedShipping: boolean,
        public sortOrder: string,
    ) {}
}
