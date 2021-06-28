//
//  CustomWebview.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/15.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import UIKit
import WebKit

class CustomWebview : WKWebView{
    var cntBackList = 0
    var history : WebViewHistory
    
    public var refreshCallback: (() -> ())?
    
    override var canGoBack: Bool {
        get {
            let result = history.backList.count > 0
            print("canGoBack : ", result)
            return  result
        }
    }
    
    override init(frame: CGRect, configuration: WKWebViewConfiguration) {
        self.history = WebViewHistory()
        super.init(frame: frame, configuration: configuration)
        
        setReload(value: "Y")
    }
    
    @objc func handleRefreshControl() { // 수정해야됨
        DispatchQueue.main.async {
//            self.reload()
            self.refreshCallback?()
            self.scrollView.refreshControl?.endRefreshing()
        }
     }

    required init?(coder: NSCoder) {
        if let history = coder.decodeObject(forKey: "history") as? WebViewHistory {
            self.history = history
        }
        else {
            history = WebViewHistory()
        }

        super.init(coder: coder)
        
        self.history = backForwardList as! WebViewHistory
    }
    override func load(_ request: URLRequest) -> WKNavigation? {
        let fields = request.allHTTPHeaderFields
        print("fields.count : ", fields)
        return super .load(request)
    }
    
    override func goBack() -> WKNavigation? {
        return super .goBack()
    }
    
    override func go(to item: WKBackForwardListItem) -> WKNavigation? {
        super .go(to: item)
    }
    
    func setReload(value : String){
        if value.elementsEqual("Y"){
            self.scrollView.refreshControl = UIRefreshControl()
            self.scrollView.refreshControl?.addTarget(self, action: #selector(handleRefreshControl), for: .valueChanged)
        }else{
            self.scrollView.refreshControl = nil
        }
    }
    
    func clearBackList() {
        cntBackList = 0
        history.backList.removeAll()
    }
    
}

class WebViewHistory : WKBackForwardList{
    override var backItem: WKBackForwardListItem? {
        return nil
    }

    /* Solution 2: override backList and forwardList to add a setter */
    var myBackList = [WKBackForwardListItem]()

    override var backList: [WKBackForwardListItem] {
        get {
            print("get  backList : " ,  myBackList.count)
            return myBackList
        }
        set(list) {
            myBackList = list
            print("set backList : " ,  myBackList.count)
        }
    }
    
    func removeLastUrl(){
        if backList.count > 0 {
            backList.remove(at: backList.count - 1)
        }
    }
}
