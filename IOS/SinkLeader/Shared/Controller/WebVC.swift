//
//  WebVC.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/15.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import UIKit
import WebKit
import SwiftyJSON
import Toast_Swift
import Alamofire

import Photos

class WebVC: BaseVC, WKUIDelegate, WKNavigationDelegate, WKScriptMessageHandler, UINavigationControllerDelegate {
    @IBOutlet weak var vwWeb: UIView!

    var wkWeb: CustomWebview!
    var mUrl:String = ""
    
    var mParam : [String:Any] = [:]
    
    var mImgPicker: UIImagePickerController!
    var mBackDelegate : BackDelegate?
    
    var mFileCallback : String = ""
    var mFileData : [String:Any] = [:]
    
    var mFileURL : URL!
    
    var loading = true
    let wkUserContent = WKUserContentController()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        print("WebView", "viewDidLoad")
                
        initUI()
        
        if mParam.count == 0 {
            
            loadURL(url: mUrl)
        }else{
            postUrl(mUrl, mParam, wkWeb)
        }
        
        NotificationCenter.default.addObserver(self, selector: #selector(onLockPage), name: UIApplication.didBecomeActiveNotification, object: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
        
    func initUI(){
        mImgPicker = UIImagePickerController()
        mImgPicker.delegate = self
        
        let preferences = WKPreferences()
        preferences.javaScriptEnabled = true
        
        wkUserContent.add(self, name: "nativeApp")
        
        let config = WKWebViewConfiguration()
        config.processPool = m_appDelegate.commonProcessPool
        config.preferences = preferences
        config.userContentController = wkUserContent
        
        wkWeb = CustomWebview(frame: vwWeb.bounds, configuration: config)
        wkWeb.uiDelegate = self
        wkWeb.navigationDelegate = self
//        wkWeb.allowsBackForwardNavigationGestures = true //
        wkWeb.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        vwWeb.autoresizesSubviews = true
        
//        wkWeb.scrollView.delegate = self
        
        wkWeb.evaluateJavaScript("navigator.userAgent") { [weak wkWeb] (result, error) in
            if let webView = wkWeb, let userAgent = result as? String {
                webView.customUserAgent = userAgent + " " + Agent
            }
        }
        
        vwWeb.addSubview(wkWeb)
    }
    
    func viewForZooming(in scrollView: UIScrollView) -> UIView? {
        return nil
    }
    
    func loadURL(url:String){
        var request = URLRequest(url: URL(string: url)!)
//        request.cachePolicy = NSURLRequest.CachePolicy.reloadIgnoringLocalCacheData
        if !m_appDelegate.token.elementsEqual(""){
            request.addValue(m_appDelegate.token, forHTTPHeaderField: "x-token")
        }
        if !m_appDelegate.RETOKEN.elementsEqual(""){
            request.addValue(m_appDelegate.RETOKEN, forHTTPHeaderField: "x-refresh-token")
        }
        if !m_appDelegate.USER_SEQ.elementsEqual(""){
            request.addValue(m_appDelegate.USER_SEQ, forHTTPHeaderField: "user_seq")
        }
        
        request.addValue("IOS", forHTTPHeaderField: "Access-Device")
        request.setValue(Agent, forHTTPHeaderField: "CustomAgent")
        
        wkWeb.load(request)
    }
    
    func postUrl(_ url: String, _ params:[String:Any],_ web:WKWebView) {
        var request = URLRequest(url: URL(string: url)!)
        request.httpMethod = "POST"
        
        for var item in params{
            request.addValue((item.value as! String), forHTTPHeaderField: item.key)
        }

        if !m_appDelegate.token.elementsEqual(""){
            request.addValue("Bearer " + m_appDelegate.token, forHTTPHeaderField: "Access-Token")
        }
        request.addValue("IOS", forHTTPHeaderField: "Access-Device")
        
        wkWeb.load(request)
    }
    
    func callJavaScript(data : [String:Any]){
        let jsonStr = CommonUtil.dictionryToJsonString(dictionry: data)
        let function = "nativeCallback('\(jsonStr)');"
        
        print("function callJavaScript : " , function)
        
        self.wkWeb.evaluateJavaScript(function) { (result, error) in
            print("function ERROR!!!! : " , result, " ======== " ,error)
            if error == nil {}
        }
    }
    
    func webViewPolicy(_ webView: CustomWebview, decidePolicyFor navigationAction: WKNavigationAction) -> WKNavigationActionPolicy {
        var result = WKNavigationActionPolicy.cancel
        
        let request = navigationAction.request
        var headers = request.allHTTPHeaderFields
        let keys = headers?.keys.sorted()
        
        var isHandler = false
        for key in keys! {
            if key.elementsEqual("Access-Device") {
                isHandler = true
                break
            }
        }
        
        if !request.url!.absoluteString.contains(ServerList[Net.Index]) {
            isHandler = true
        }else if request.url!.absoluteString.contains("/common/niceauth") {
            isHandler = true
        }
        
        
        if isHandler {
            UIApplication.shared.isNetworkActivityIndicatorVisible = true
            
            if webView.backForwardList.backList.count < wkWeb.cntBackList {
                if wkWeb.canGoBack{
                    result = .allow
                }else{
                    result = .cancel
                }
            }else{
                result = .allow
            }
        }else{
            if webView.backForwardList.backList.count < wkWeb.cntBackList {
                if wkWeb.canGoBack{
                    wkWeb.history.removeLastUrl()
                    
                    print("request url : ", request.url!.absoluteString)
                    loadURL(url: request.url!.absoluteString)
                }
            }else{
                print("request url : ", request.url!.absoluteString)
                loadURL(url: request.url!.absoluteString)
            }

            result = .cancel
        }
        
        
        return result
    }
        
    //////////////////////////////////////////////////////////////////////
    // MARK: - WKUIDelegate
    //////////////////////////////////////////////////////////////////////
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        print("didFinish count : " , webView.backForwardList.backList.count)
        print("cntBackList count : " , wkWeb.cntBackList)
        
//        print("httpBody : " ,navigationAction.request.httpBody )
        
        guard navigationAction.request.url?.absoluteString.elementsEqual("about:blank") == false  else {
            decisionHandler(.cancel)
            return
        }
        
        let policy = webViewPolicy(webView as! CustomWebview, decidePolicyFor: navigationAction)
        decisionHandler(policy)
    }
        
    func webView(_ webView: WKWebView, runJavaScriptAlertPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping () -> Void) {
        MessagePopup.show(self, message, {
          completionHandler()
        })
    }
    
    func webView(_ webView: WKWebView, didCommit navigation: WKNavigation!) {
//        print("didCommit url : ",  webView.url?.absoluteString)
        if wkWeb.cntBackList > webView.backForwardList.backList.count {
            if wkWeb.canGoBack {
                wkWeb.history.removeLastUrl()
            }
        }else{
            if webView.backForwardList.backList.count > 0{
                wkWeb.history.backList.append(webView.backForwardList.backItem!)
            }
        }
        
        if (webView.url?.absoluteString.contains(Net.MainPage_URL))! {
            wkWeb.clearBackList()
        }
        wkWeb.cntBackList = webView.backForwardList.backList.count
    }
    
    
    func webView(_ webView: WKWebView, runJavaScriptConfirmPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (Bool) -> Void) {
        let alert = MsgUtil.create(message, ok: "승인", cancel: "취소", okCallback: {
            completionHandler(true)
        }, cancelCallback: {
            completionHandler(false)
        })

        DispatchQueue.main.async(execute: { () -> Void in
            self.present(alert, animated: true)
        })
    }

    
    
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        if message.name == "nativeApp"{
            print("nativeApp", message.body)
            
            if let body = message.body as? [String:Any] {
                print("nativeApp message : ", body.description)

                let method = body["method"] as! String
                let callback = body["callbackMethod"] as! String
                let data = body["data"] as! [String:Any]
                
                actionNativeCall(method: method, callback: callback, data: data)
            }
        }
    }
    
    @IBAction func swipeAction(_ gesture: UISwipeGestureRecognizer) {
        print("swipeAction")
        if gesture.direction == .right {
            if wkWeb.canGoBack {
                wkWeb.goBack()
            }
        }
    }
    
}

extension WebVC {
    //  사진 업로드
    func showPhotoAlert(callback : String, data : [String:Any] ) {
        mFileCallback = callback
        mFileData = data
        
        let acAlert = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        acAlert.addAction(UIAlertAction(title: "앨범", style: .default){ (action) in
            self.authorize(fromViewController: self) { (authorized) -> Void in
                guard authorized else {
                    self.showDialog()
                    return
                }
                self.showGallery()
            }
        })
        acAlert.addAction(UIAlertAction(title: "카메라", style: .default){ (action) in
            if AVCaptureDevice.authorizationStatus(for: .video) ==  .authorized {
                self.showCamera()
            } else {
                
                AVCaptureDevice.requestAccess(for: .video, completionHandler: { (granted: Bool) in
                    DispatchQueue.main.async(execute: { () -> Void in
                        let json : [String : Any] = [:]
                        let todata : [String : Any] = ["callbackMethod" : self.mFileCallback,
                                                       "data" : json]
                        self.callJavaScript(data: todata)
                        
                        if granted {
                            self.showCamera()
                        } else {
                            self.showDialog()
                        }
                    })
                })
            }
        })
        acAlert.addAction(UIAlertAction(title: "취소", style: .cancel){(action) in
//            self.callCancelJavaScript(data: self.mFileData)
            let json : [String : Any] = [:]
            let todata : [String : Any] = ["callbackMethod" : self.mFileCallback,
                                           "data" : json]
            self.callJavaScript(data: todata)
        })
        present(acAlert, animated: true, completion: nil)
    }
    
    func showGallery() {
        mImgPicker.sourceType = .photoLibrary
        mImgPicker.allowsEditing = false
        mImgPicker.mediaTypes = ["public.image"]
        present(mImgPicker, animated: true, completion: nil)
    }
    
    func showCamera() {
        if UIImagePickerController.isSourceTypeAvailable(.camera) {
            mImgPicker.sourceType = .camera
            mImgPicker.mediaTypes = ["public.image"]
            mImgPicker.allowsEditing = false
            mImgPicker.showsCameraControls = true
            present(mImgPicker, animated: true, completion: nil)
        } else {
            showPopup(dlg: MessagePopup("카메라를 지원하지 않습니다. 다시 확인해 주세요."))
//            self.callCancelJavaScript(data: self.mFileData)
            let json : [String : Any] = [:]
            let todata : [String : Any] = ["callbackMethod" : self.mFileCallback,
                                           "data" : json]
            self.callJavaScript(data: todata)
        }
    }
    
    func authorize(_ status: PHAuthorizationStatus = PHPhotoLibrary.authorizationStatus(), fromViewController: UIViewController, completion: @escaping (_ authorized: Bool) -> Void) {
        switch status {
        case .authorized:             // We are authorized. Run block
            completion(true)
            
        case .notDetermined:             // Ask user for permission
            PHPhotoLibrary.requestAuthorization({ (status) -> Void in
                DispatchQueue.main.async(execute: { () -> Void in
                    self.authorize(status, fromViewController: fromViewController, completion: completion)
                })
            })
            
        default:
            DispatchQueue.main.async(execute: { () -> Void in
                completion(false)
            })
        }
//        self.callCancelJavaScript(data: self.mFileData)
    }
    func showDialog() {
        let alert = UIAlertController (title: "권한 요청", message: "해당 기능을 사용하려면 권한이 필요합니다.\n권한을 허용해주세요.", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "취소", style: .cancel, handler: nil))
        alert.addAction(UIAlertAction(title: "Settings", style: .destructive) { void in
            self.goSetting()
        })
        
        
        DispatchQueue.main.async(execute: { () -> Void in
            self.present(alert, animated: true, completion: nil)
//            self.callCancelJavaScript(data: self.mFileData)
        })
    }
    func goSetting() {
        if let url = URL(string: UIApplication.openSettingsURLString) {
            CommonUtil.loadUrl(url)
        }
    }
    
    func requestRefesh(callback : String){
        let url = ServerList[Net.Index] + "/v1/main/common/token"
        
        let param = ["x_token": m_appDelegate.token] as [String:Any]

        Net.doRequest(method: .get, api: url, params: param, success: {
            result in
            print("requestRefesh Suc : \(result)")
            
            let code = result!["result"].numberValue
            if code == 0 {
                let message = result!["message"].stringValue
                let data = result!["data"]
                let user = data["user"]
                
                self.m_appDelegate.token = user["token"].stringValue
                self.m_appDelegate.RETOKEN = user["refresh_token"].stringValue
                self.m_appDelegate.USER_SEQ = user["user_seq"].stringValue
                self.m_appDelegate.USER_NAME = user["name"].stringValue
                self.m_appDelegate.USER_IMG = user["profile_img_url"].stringValue
                self.m_appDelegate.USER_LASTDATE = user["last_access_date"].stringValue
                
                let json : [String : Any] = ["token" : self.m_appDelegate.token,
                                             "refresh_token" : self.m_appDelegate.RETOKEN,
                                             "user_seq" : self.m_appDelegate.USER_SEQ,
                                             "name" : self.m_appDelegate.USER_NAME,
                                             "profile_img_url" : self.m_appDelegate.USER_IMG,
                                             "last_access_date":self.m_appDelegate.USER_LASTDATE]
                let todata : [String : Any] = ["callbackMethod" : callback,
                                               "data" : json]

                self.callJavaScript(data: todata)
            }else{
                let msg = result!["message"].stringValue
                MessagePopup.show(self, msg)
            }
        }, failure: {(code, msg) in
            print("requestRefesh Err : \(code) \(msg)")
        })
    }
}

extension WebVC : Actions{
    func actionNativeCall(method : String, callback: String, data: [String:Any]){
        print("function actionNativeCall : ", method)

        if method.elementsEqual("openWebView"){
            var page_url = data["url"] as! String
            page_url = page_url.removingPercentEncoding!
            
//            let _url = page_url.addingPercentEncoding(withAllowedCharacters: .u)!
            
            let vc = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "WEB_VC") as! WebVC
            vc.mUrl = page_url
            vc.mBackDelegate = self
            self.present(vc, animated: true)
            
        }else if method.elementsEqual("changeUrlWebView"){
            var page_url = data["url"] as! String
            page_url = page_url.removingPercentEncoding!
            loadURL(url: page_url)
            
        }else if method.elementsEqual("isReload"){
//            var value = data["value"] as! String
//            wkWeb.setReload(value: value)
        
        }else if method.elementsEqual("closeWebView"){
//            let page_url = data["url"] as! String?
            self.dismiss(animated: false, completion: {
                if self.mBackDelegate != nil {
                    // 임시
                    let page_url = data["url"] as! String?
                    var datas: [String:Any] = [:]
                    if !callback.elementsEqual("") {
                        datas = ["callbackMethod" : "\(callback)", "data": data]
                    }
                    if (page_url == nil || page_url!.count < 1) && datas.count < 1 {
                        return
                    }
                    
                    var _url = ""
                    
                    if page_url != nil {
                        _url = page_url!.addingPercentEncoding(withAllowedCharacters: .urlFragmentAllowed)!
                    }
                    
                    self.mBackDelegate?.callback(_url, datas)
                }
            })
            
        }else if method.elementsEqual("getUserInfo"){
            var deviceInfo = getDeviceInfo()
            
            deviceInfo.updateValue(m_appDelegate.token, forKey: "token")
            deviceInfo.updateValue(m_appDelegate.RETOKEN, forKey: "refresh_token")
            
            deviceInfo.updateValue(m_appDelegate.USER_NAME, forKey: "user_name")
            deviceInfo.updateValue(m_appDelegate.USER_SEQ, forKey: "user_seq")
            deviceInfo.updateValue(m_appDelegate.USER_LASTDATE, forKey: "last_access_date")
            deviceInfo.updateValue(m_appDelegate.USER_IMG, forKey: "profile_img_url")
            deviceInfo.updateValue(m_appDelegate.CENTER_SEQ, forKey: "center_seq")
            deviceInfo.updateValue(m_appDelegate.USER_TYPE, forKey: "user_type")
            deviceInfo.updateValue(m_appDelegate.USER_GRADE, forKey: "app_user_grade")
            
            let data = ["callbackMethod" : "\(callback)", "data": deviceInfo] as [String:Any]
            self.callJavaScript(data: data)
            
        }else if method.elementsEqual("setToken"){
            m_appDelegate.token = data["token"] as! String
            m_appDelegate.RETOKEN = data["refresh_token"] as! String
            
        }else if method.elementsEqual("getToken"){
            requestRefesh(callback: callback)

        }else if method.elementsEqual("openBrowser"){
            let page_url = data["url"] as! String
            let urls = URL(string: page_url)!
            UIApplication.shared.open(urls, options: [:], completionHandler: nil)
            
        }else if method.elementsEqual("Logout"){
//            showPopup(dlg: DialogPopup2("로그아웃 하시겠습니까?", {
//
//            }))
            self.m_appDelegate.isAutoLogin = true
            self.m_appDelegate.token = ""
            self.m_appDelegate.isAutoLogin = false
            self.m_appDelegate.RETOKEN = ""
            self.m_appDelegate.USER_NAME = ""
            self.m_appDelegate.USER_IMG = ""
            self.m_appDelegate.USER_SEQ = ""
            
            let vc = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "LOGIN_VC") as! LoginVC
            self.present(vc, animated: true)
            
        }else if method.elementsEqual("goBack"){
            self.TestBack("")
            
        }else if method.elementsEqual("getGPS"){
            settingGps(callbackMethod: callback)
            
        }else if method.elementsEqual("openTel"){
            let callnum =  data["tel"] as! String

            if let phoneURL = URL(string: "tel://" + callnum){
                let app : UIApplication = UIApplication.shared

                if(app.canOpenURL(phoneURL)){
                    app.open(phoneURL, options: [:])
                }
            }
        }else if method.elementsEqual("openScanner"){
            let name =  data["name"] as! String
            
            let vc = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "SCAN_VC") as! QRScanVC
            vc.method = callback
            vc.name = name
            vc.cbFunction = callbackQR
            self.present(vc, animated: true)
        }else if method.elementsEqual("openSignature"){
            let vc = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "SIGN_VC") as! SignatureVC
            vc.method = callback
            vc.cbFunction = callbackSign
            self.present(vc, animated: true)
        }else if method.elementsEqual("openNaverMap"){
            let address = data["address"] as! String
            let schemeURL = URL(string: "nmap://search?query=\(address)")!
            
            if UIApplication.shared.canOpenURL(schemeURL){
                print(schemeURL)
                UIApplication.shared.open(schemeURL, options: [:], completionHandler: {
                    (bool) -> Void in
                    print(bool)
                })
            }
        }else if method.elementsEqual("actionShare"){
            let title = data["title"] as? String
            let url = data["url"] as? String
            
            self.shareItem(title: title!, url: url!)
            
            
        }else if method.elementsEqual("getPhoto"){
            showPhotoAlert(callback: callback, data: data)
            
        }else if method.elementsEqual("getFile"){
            
        }else if method.elementsEqual("downLoad"){
            let url = data["url"] as! String
            let title = data["title"] as! String
            self.download(fileName: title, url: URL(string: url)!)
            
        }else if (method.elementsEqual("reLoadPage")) { //
            wkWeb.reload()
            
        }
    }

    func shareItem(title:String, url:String){
        print("announce call")
         
         let textToShare = [ title, url ]
         // 액티비티 뷰 컨트롤러 셋업
         let activityVC = UIActivityViewController(activityItems: textToShare, applicationActivities: nil)
         // 제외하고 싶은 타입을 설정 (optional)
        activityVC.excludedActivityTypes = [.airDrop]
         // 현재 뷰에서 present
         self.present(activityVC, animated: true, completion: nil)
    }
    
    @IBAction func TestBack(_ sender: Any) {
        if wkWeb.canGoBack {
            let tt = wkWeb.backForwardList.backList
            
            for var t in tt {
                print(t.url)
            }
            
            wkWeb.goBack()
        }else if !(wkWeb.url?.absoluteString.contains(Net.MainPage_URL) ?? false){
            initUI()
            loadURL(url: Net.MainPage_URL)
        }
    }
}

extension WebVC: UIDocumentMenuDelegate, UIDocumentPickerDelegate, UIImagePickerControllerDelegate {
//    func selectFile(callback : String, data : [String:Any]) {
//        mFileCallback = callback
//        mFileData = data
//
//        let impMenu = UIDocumentMenuViewController(documentTypes: ["public.data"], in: .import)
//        if #available(iOS 13.0, *) {
//            impMenu.addOption(withTitle: "사진", image: UIImage(systemName: "photo.on.rectangle"), order: .first, handler: {
//                self.showPhotoAlert(callback: callback, data: data)
//            })
//        } else {
//            // Fallback on earlier versions
//            impMenu.addOption(withTitle: "사진", image: UIImage(named:"photo.on.rectangle"), order: .first, handler: {
//                self.showPhotoAlert(callback: callback, data: data)
//            })
//        }
//        impMenu.delegate = self
//        impMenu.modalPresentationStyle = .formSheet
//        self.present(impMenu, animated: true, completion: nil)
//    }
    
    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentAt url: URL) {
        /// Handle your document
        print("documentPicker : ", url.absoluteString)
        
        let data = try? Data(contentsOf: url)
        //mimeType
        
        let fileName = "" + url.absoluteString.split(separator: "/").last!
        let mimeType = CommonUtil.mimeType(string: url.absoluteString.split(separator: ".").last!.description)
                
        fileUpload(fileName: fileName, mimeType: mimeType, data: data!)
    }
    
    func documentMenuWasCancelled(_ documentMenu: UIDocumentMenuViewController) {
        print("documentMenuWasCancelled")
        
        let json : [String : Any] = [:]
        let todata : [String : Any] = ["callbackMethod" : callback,
                                       "data" : json]
        self.callJavaScript(data: todata)
    }
    
    func documentMenu(_ documentMenu: UIDocumentMenuViewController, didPickDocumentPicker documentPicker: UIDocumentPickerViewController) {
        documentPicker.delegate = self
        documentPicker.modalPresentationStyle = .overCurrentContext
        present(documentPicker, animated: true, completion:{
            print("documentPicker ssss")
        })
    }
    func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        /// Picker was cancelled! Duh 🤷🏻‍♀️
//        self.callCancelJavaScript(data: self.mFileData)
    }
    
    //////////////////////////////////////////////////////////////////////
    // MARK: - UIImagePickerControllerDelegate
    //////////////////////////////////////////////////////////////////////
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        dismiss(animated: true){
            print("imagePickerControllerDidCancel")
        }
        
        if self.loading {
            let json : [String : Any] = [:]
            let todata : [String : Any] = ["callbackMethod" : self.mFileCallback,
                                           "data" : json]
            self.callJavaScript(data: todata)
        }
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        loading = false
        imagePickerControllerDidCancel(picker)
        
        loading = true
        if let image = info[.originalImage] as? UIImage {
            let data = image.pngData()
            
            var fileName = "cameraImage.png"
            var mimeType = "image/png"
            
            if let url = info[UIImagePickerController.InfoKey.imageURL] as? URL{
                fileName = "" + url.absoluteString.split(separator: "/").last!
                mimeType = CommonUtil.mimeType(string: url.absoluteString.split(separator: ".").last!.description)
            }

            fileUpload(fileName: fileName, mimeType: mimeType, data: data!)
        }
    }
    
    func fileUpload(fileName: String, mimeType: String, data:Data) {
        let server_url = mFileData["url"] as! String
        let fileMark = mFileData["paramName"] as! String
        let thumbnailMake = mFileData["thumbnailMake"] as! String
        let ind = mFileData["ind"] as! String
        
        let param : [String:Any] = ["file_name":fileMark,
                                    "thumbnail_type":thumbnailMake,
                                    "ind":ind]

        Net.doRequestForFile(method: .post, url: server_url, filename: fileName, mimeType: mimeType, fileArray: [data], fileMark: fileMark, params: param, success: {
            result in
            print("fileUpload Suc : \(result)")
//            self.callCancelJavaScript(data: self.mFileData)
            
            let code = result!["result"].numberValue
            if code == 0 {
                let message = result!["message"].stringValue
                let data = try? (JSONSerialization.jsonObject(with: result!["data"].rawData(), options: []) as! [String:Any])
                let json : [String : Any] = ["data" : data as Any, "result" : code, "message" : message]
                
                let todata : [String : Any] = ["callbackMethod" : self.mFileCallback,
                                               "data" : json]
                self.callJavaScript(data: todata)
                
                
            }else{
                let msg = result!["message"].stringValue
                MessagePopup.show(self, msg)
                let json : [String : Any] = [:]
                let todata : [String : Any] = ["callbackMethod" : self.mFileCallback,
                                               "data" : json]
                self.callJavaScript(data: todata)
            }
        }, failure: {(code, msg) in
            let json : [String : Any] = [:]
            let todata : [String : Any] = ["callbackMethod" : self.mFileCallback,
                                           "data" : json]
            self.callJavaScript(data: todata)
            print("fileUpload Err : \(code) \(msg)")
        })
    }
}

extension WebVC{
    func settingGps(callbackMethod: String) {
        var callback = callbackMethod
        if callback.elementsEqual("") {
            callback = "callbackGps"
        }
        
        let gps = m_appDelegate.myGps;
        let location = gps.split(separator: ",")
        //nativeCallback('{"callbackMethod":"callbackGps","data":{"latitude":"37.5143547","longitude":"127.0417981"}}')
//        location[1] = "37.516165967679086"
//        location[0] = "127.04205985080215"
        
        let data = ["callbackMethod" : "\(callback)", "data": ["latitude":"\(location[1])", "longitude":"\(location[0])"] as [String:Any]] as [String:Any]
        
        callJavaScript(data: data)
    }
    
    func download(fileName:String, url: URL){
        let fileManager = FileManager.default
        let documentsURL  = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
        mFileURL = documentsURL.appendingPathComponent(fileName)
        
        print("documentsURL : ", mFileURL)

        let sessionConfiguration = URLSessionConfiguration.default
        let session = URLSession(configuration: sessionConfiguration)
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Bearer " + m_appDelegate.token, forHTTPHeaderField: "Access-Token")
        request.addValue("IOS", forHTTPHeaderField: "Access-Device")
        
        let downloadTask = session.downloadTask(with: request){
            (tempLoaalURL, response, error) in
            try? fileManager.removeItem(at: self.mFileURL)
            do {
                try fileManager.copyItem(at: tempLoaalURL!, to: self.mFileURL)
                
                DispatchQueue.main.async(execute: { () -> Void in
                    MessagePopup.show(self, "\(fileName)가 다운로드가 완료되었습니다.")
                })
            }catch (let writeError){
                print("err \(self.mFileURL) : \(writeError)")
            }
        }
        downloadTask.resume()
    }
}

extension WebVC : BackDelegate {
    func callback(_ url : String? = "",_ data : [String:Any] = [:]) {
        
        if data.count > 0 {
            callJavaScript(data: data)
        }
        
        if !(url?.elementsEqual("") ?? true) {
            loadURL(url: url!)
        }
    }
    
    func callbackQR(method:String, barcode:String, isScan:Bool) {
        print("barcode: \(barcode), isScan: \(isScan)")
        
        let info = ["barcode" : "\(barcode)", "is_scan": isScan] as [String:Any]
        let data = ["callbackMethod" : "\(method)", "data": info] as [String:Any]
        self.callJavaScript(data: data)
    }
    
    func callbackSign(method:String, svg_str:String) {
        print("svg: \(svg_str)")
        let info = ["svg" : "\(svg_str)"] as [String:Any]
        let data = ["callbackMethod" : "\(method)", "data": info] as [String:Any]
        self.callJavaScript(data: data)
    }
}
