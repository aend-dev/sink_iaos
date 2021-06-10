//
//  AppDelegate.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/15.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import UIKit
import Alamofire
import SwiftyJSON

let Agent = "SinkLeader-Mobile"

let ServerList = [
    "https://api.allbabo.com",
    "http://allba-api.dev.aend.co.kr",
    "http://192.168.29.248:30000",
    "http://192.168.29.185:8120"
]

let FrontList = [
    "https://front.allbabo.com",
    "http://allba-front.dev.aend.co.kr",
    "http://office.aend.co.kr:20022",
    "http://192.168.29.248:10001",
]

class Net {
//    static var ServerAPI_Url =    Bundle.main.infoDictionary!["Url_Api"] as! String
    static var Index = 0
    static var FrontIndex = 0
    private static var commonHeaders : HTTPHeaders = [:]
    
    class func setCommonHeaders(headers : HTTPHeaders) {
        commonHeaders = headers
    }
    
    static var WebUrl_Url          = ""
    static var MainPage_URL        = ""
//    static var PUSH_RECEIVE_URL    = ""
    static var SIGNUP_URL          = ""

    public static func changeServer(_ val: Int){
        Index = val
        FrontIndex = val
        WebUrl_Url = ServerList[Index]
        MainPage_URL    = WebUrl_Url + "/home"
    }

    
    private class func convVal(_ val: Bool) -> String {
        return val ? "Y" : "N"
    }
    
    //
    // MARK: API response structure
    //
    typealias SuccessBlock = (JSON?) -> Void
    typealias FailureBlock = (_ code: Int, _ error: String) -> Void
    
    class ResponseResult {
        
    }
    
    class StatusResult: ResponseResult {
        var msg: String!
    }
    
    //
    // MARK: Helper functions
    //
    
    /**
     *  HTTP request.
     */
    public class func doRequest(
        method        : Alamofire.HTTPMethod,
        api            : String,
        params        : [String: Any]?,
        header       : HTTPHeaders = [:],
        success        : SuccessBlock?,
        failure        : FailureBlock?,
        sub_url        : String = ""
        )
    {
        let url = api + "?nocache=\(arc4random())" + "&" + sub_url
        print("\n", url)
        print(params ?? "[]")
        
        AF.request(url, method: method, parameters: params, encoding: URLEncoding.default, headers: commonHeaders).responseJSON { response in
            switch response.result {
            case .failure(let error):
                if let failure = failure {
                    let res = error.localizedDescription
                    print("\nAPI Call Failed!\nURL : \(url)\nError : \(res)")
                    failure(-999, "The server connection status is unstable. Please try again.")
                }
                return
                
            case .success(let json):
                var res = JSON(json)
                print(res)
                
                do {
                    if let success = success {
                        success(res)
                    }
                } catch _ {
                    if let failure = failure {
                        failure(-900, "Fail to parse server response(invalid object)")
                    }
                }
            }
        }
    }
    
    /**
     *  HTTP request.
     */
    public class func doRequestCustomHeader(
        method        : Alamofire.HTTPMethod,
        api            : String,
        params        : [String: Any]?,
        header       : HTTPHeaders = [:],
        success        : SuccessBlock?,
        failure        : FailureBlock?,
        sub_url        : String = ""
        )
    {
        let url = api + "?nocache=\(arc4random())" + "&" + sub_url
        print("\n", url)
        print(params ?? "[]")
        
        AF.request(url, method: method, parameters: params, encoding: URLEncoding.default, headers: header).responseJSON { response in
            switch response.result {
            case .failure(let error):
                if let failure = failure {
                    let res = error.localizedDescription
                    print("\nAPI Call Failed!\nURL : \(url)\nError : \(res)")
                    failure(-999, "The server connection status is unstable. Please try again.")
                }
                return
                
            case .success(let json):
                var res = JSON(json)
                print(res)
                
                do {
                    if let success = success {
                        success(res)
                    }
                } catch _ {
                    if let failure = failure {
                        failure(-900, "Fail to parse server response(invalid object)")
                    }
                }
            }
        }
    }
    
    
    public class func doRequestJson(
        method        : Alamofire.HTTPMethod,
        api            : String,
        params        : [String: Any]?,
        success        : SuccessBlock?,
        failure        : FailureBlock?,
        token       : String = "",
        gps         : String = "",
        sub_url     : String = ""
        )
    {
        let url = api + sub_url + "?nocache=\(arc4random())"
        print("\n", url)
        print(params ?? "[]")
        
        let headers : HTTPHeaders = ["Content-Type":"application/json", "Accept":"application/json"]
        
        AF.request(url, method: method, parameters: params, encoding: JSONEncoding.default, headers: headers)
            .responseJSON { response in //JSONEncoding
            switch response.result {
            case .failure(let error):
                if let failure = failure {
                    let res = error.localizedDescription
                    print("\nAPI Call Failed!\nURL : \(url)\nError : \(res)")
                    failure(-999, "The server connection status is unstable. Please try again.")
                }
                return
                
            case .success(let json):
                var res = JSON(json)
                print(res)
                
                do {
                    if let success = success {
                        success(res)
                    }
                } catch _ {
                    if let failure = failure {
                        failure(-900, "Fail to parse server response(invalid object)")
                    }
                }
            }
        }
    }
    
    /**
     *  specific HTTP request.
     */
    private class func doRequestForSpec(
        method        : Alamofire.HTTPMethod,
        api            : String,
        params        : [String: Any]?,
        headers        : HTTPHeaders,
        success        : SuccessBlock?,
        failure        : FailureBlock?
        )
    {
        print(api)
        AF.request(api, method: method, parameters: params, encoding: JSONEncoding.default, headers: headers).responseJSON { response in
            switch response.result {
            case .failure(let error):
                if let failure = failure {
                    print("\nAPI Call Failed!\nError : \(error.localizedDescription)")
                    failure(-999, "The server connection status is unstable. Please try again.")
                }
                return
                
            case .success(let json):
                let res = JSON(json)
                print(res)
            }
        }
    }
    
    /**
     *  File request.
     */
    public class func doRequestForFile (
        method            : Alamofire.HTTPMethod,
        url               : String,
        filename          : String,
        mimeType          : String,
        fileArray         : [Data]! = [],
        fileMark          : String = "file",
        thumbnailMake     : Bool = false,
        params            : [String: Any]?,
        success           : SuccessBlock?,
        failure           : FailureBlock?
        )
    {
        let urls = url + "?nocache=\(arc4random())"
        print(urls)
        print(params ?? "[]")
        
//        let headers : HTTPHeaders = [:]
        
        AF.upload(multipartFormData:{ (MultipartFormData) in
            if fileArray.count > 0 {
                for i in 0...fileArray.count - 1 {
                    let strName = fileMark
                    MultipartFormData.append(fileArray[i], withName: strName, fileName: filename, mimeType: mimeType)
                }
            }
            
            for (key, value) in params! {
                let data = JSON(value).stringValue
                MultipartFormData.append(data.data(using: String.Encoding.utf8)!, withName: key)
            }
        }, to: url).responseJSON{ (response) in
            print(response)
            
            switch response.result {
            case .failure(let error):
                if let failure = failure {
                    let res = error.localizedDescription
                    print("\nAPI Call Failed!\nURL : \(url)\nError : \(res)")
                    failure(-999, "The server connection status is unstable. Please try again.")
                }
                return
                
            case .success(let json):
                var res = JSON(json)
                print(res)
                
                do {
                    if let success = success {
                        success(res)
                    }
                } catch _ {
                    if let failure = failure {
                        failure(-900, "Fail to parse server response(invalid object)")
                    }
                }
            }
        }
    }
}
