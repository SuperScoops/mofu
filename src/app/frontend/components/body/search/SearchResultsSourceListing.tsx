import React from 'react'
import autobind from 'autobind'
import { SearchResult } from 'backend/search'
import accessor from 'backend/utils/accessor'
import { MangaSource } from 'backend/sources'
import { MangaInfo } from 'backend/abstracts'
import { SearchResultsSeriesListing } from 'frontend/components'

interface Props {
    results: SearchResult[],
    sourceName: string,
    callback: (info: MangaInfo) => void,
    source: MangaSource
}

let counter = 0;

export default class SearchResultSourceListing extends React.Component<Props, any> {
    constructor(props) {
        super(props)
    }

    public render() {
        let listings = []
        this.props.results.forEach(res => {
            listings.push(<SearchResultsSeriesListing title={res.title} url={res.url} source={this.props.source} altNames={res.altNames} key={++counter} callback={this.props.callback} />)
        });
        return (
            <div className="search-results">
                <div className="search-source">
                    <div className="search-source-name">
                        <div className="toggle left"><i className="fa fa-angle-double-up"></i></div>
                        <a href="#">{this.props.sourceName + ' (' + this.props.results.length + ') '}</a>
                        <div className="toggle right"><i className="fa fa-angle-double-up"></i></div>
                    </div>
                    {listings}
                </div>
            </div>
        )
    }
}
