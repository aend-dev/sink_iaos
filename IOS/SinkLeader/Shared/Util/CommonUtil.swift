
import Foundation
import UIKit
import SwiftKeychainWrapper

class CommonUtil {
    
    static func bundleID() -> String {
        return Bundle.main.infoDictionary!["CFBundleIdentifier"] as! String
    }
    
    static func bundleVer() -> String {
        return Bundle.main.infoDictionary!["CFBundleShortVersionString"] as! String
    }
    
    static func systemVer() -> String {
        return UIDevice.current.systemVersion
    }
    
    static func buildNum() -> String {
        return Bundle.main.infoDictionary!["CFBundleVersion"] as! String
    }
    
    static func modelName() -> String {
        var systemInfo = utsname()
        uname(&systemInfo)
        let machineMirror = Mirror(reflecting: systemInfo.machine)
        let identifier = machineMirror.children.reduce("") { identifier, element in
            guard let value = element.value as? Int8, value != 0 else { return identifier }
            return identifier + String(UnicodeScalar(UInt8(value)))
        }
        
        switch identifier {
        case "iPod5,1":                                 return "iPod Touch 5"
        case "iPod7,1":                                 return "iPod Touch 6"
        case "iPhone3,1", "iPhone3,2", "iPhone3,3":     return "iPhone 4"
        case "iPhone4,1":                               return "iPhone 4s"
        case "iPhone5,1", "iPhone5,2":                  return "iPhone 5"
        case "iPhone5,3", "iPhone5,4":                  return "iPhone 5c"
        case "iPhone6,1", "iPhone6,2":                  return "iPhone 5s"
        case "iPhone7,2":                               return "iPhone 6"
        case "iPhone7,1":                               return "iPhone 6 Plus"
        case "iPhone8,1":                               return "iPhone 6s"
        case "iPhone8,2":                               return "iPhone 6s Plus"
        case "iPhone9,1", "iPhone9,3":                  return "iPhone 7"
        case "iPhone9,2", "iPhone9,4":                  return "iPhone 7 Plus"
        case "iPhone8,4":                               return "iPhone SE"
        case "iPad2,1", "iPad2,2", "iPad2,3", "iPad2,4":return "iPad 2"
        case "iPad3,1", "iPad3,2", "iPad3,3":           return "iPad 3"
        case "iPad3,4", "iPad3,5", "iPad3,6":           return "iPad 4"
        case "iPad4,1", "iPad4,2", "iPad4,3":           return "iPad Air"
        case "iPad5,3", "iPad5,4":                      return "iPad Air 2"
        case "iPad2,5", "iPad2,6", "iPad2,7":           return "iPad Mini"
        case "iPad4,4", "iPad4,5", "iPad4,6":           return "iPad Mini 2"
        case "iPad4,7", "iPad4,8", "iPad4,9":           return "iPad Mini 3"
        case "iPad5,1", "iPad5,2":                      return "iPad Mini 4"
        case "iPad6,3", "iPad6,4", "iPad6,7", "iPad6,8":return "iPad Pro"
        case "AppleTV5,3":                              return "Apple TV"
        case "i386", "x86_64":                          return "Simulator"
        default:                                        return identifier
        }
    }
    
    static func deviceName() -> String {
        return UIDevice.current.name
    }
    
    static func deviceUUID() -> String! {
        let id_key = "_UUID"
        var stored = KeychainWrapper.standard.string(forKey: id_key)
        
        if stored == nil {
            let theUUID = CFUUIDCreate(nil)
            let cfstr = CFUUIDCreateString(nil, theUUID)
            let nsTypeString = cfstr!
            let swiftString = nsTypeString as String
            
            _ = KeychainWrapper.standard.set(swiftString, forKey: id_key)
            stored = KeychainWrapper.standard.string(forKey: id_key)
        }
        
        if stored == nil {
            stored = "undefined"
        }
        
        // print("DeviceID:\(stored)")
        return stored
    }
    
    static func topViewController() -> UIViewController? {
        var topVC = UIApplication.shared.keyWindow!.rootViewController
        while (topVC != nil && topVC?.presentedViewController != nil) {
            topVC = topVC?.presentedViewController
        }
        return topVC
    }
    
    static func validEmail(_ exp: String) -> Bool {
        let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        
        let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailTest.evaluate(with: exp)
    }
    
    static func validUrl(_ string: String?) -> Bool {
        if string != nil, let url = URL(string: string!) {
            return UIApplication.shared.canOpenURL(url as URL)
        }
        return false
    }
    
    static func loadUrl(_ url: String) {
        if validUrl(url) {
            if #available(iOS 10.0, *) {
                UIApplication.shared.open(URL(string: url)!)
            } else {
                UIApplication.shared.openURL(URL(string: url)!)
            }
        }
    }
    
    static func validUrl(_ url: URL) -> Bool {
        return UIApplication.shared.canOpenURL(url)
    }
    
    static func loadUrl(_ url: URL) {
        if validUrl(url) {
            if #available(iOS 10.0, *) {
                UIApplication.shared.open(url)
            } else {
                UIApplication.shared.openURL(url)
            }
        }
    }
        
    static func paramFromUrl(url: String, param: String) -> String? {
        guard let url = URLComponents(string: url) else { return nil }
        return url.queryItems?.first(where: { $0.name == param })?.value
    }
    
    static func loadApp(id: String) {
        let url = "itms-apps://itunes.apple.com/app/" + id
        loadUrl(url)
    }
    
    static func loadApp(url: String) {
        let _url = url.addingPercentEncoding(withAllowedCharacters: .urlFragmentAllowed)!
        loadUrl(_url)
    }
	
    static func formatNum(_ val: Int) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = NumberFormatter.Style.decimal
        return formatter.string(from: NSNumber(value: val))!
    }
    
    static func formatNum(_ val: Float) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = NumberFormatter.Style.decimal
        return formatter.string(from: NSNumber(value: val))!
    }
    
    static func phaseFullName(fullName: String!) -> [String] {
        var arrRet = ["", ""]
        
        if fullName != nil && fullName != "" {
            arrRet = fullName.split{ $0 == " " }.map(String.init)
        }
        
        return arrRet
    }
    
    static func dictionryToJsonString(dictionry : [String:Any]) -> String {
        var result = ""
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: dictionry, options: .prettyPrinted)
            let convertedString = String(data: jsonData, encoding: String.Encoding.utf8)
            result = convertedString!.replacingOccurrences(of: "\n", with: "")
            result = result.replacingOccurrences(of: " ", with: "")
        } catch {
            print(error)
        }
        
        return result
    }
    
    static func containsOnlyLettersAndNumbers(_ input: String) -> Bool {
        for chr in input {
            if !(chr >= "a" && chr <= "z") && !(chr >= "A" && chr <= "Z") && !(chr >= "0" && chr <= "9") {
                return false
            }
        }
        return true
    }
    
    static func addDoneButton(_ textField: UITextField) {
        let keypadToolbar = UIToolbar()
        
        keypadToolbar.items = [
            UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: self, action: nil),
            UIBarButtonItem(title: "Done", style: .done, target: textField, action: #selector(UITextField.resignFirstResponder)),
        ]
        keypadToolbar.sizeToFit()
        textField.inputAccessoryView = keypadToolbar
    }
    
    static func addNextButton(_ textField: UITextField, to nextTextField: UITextField) {
        let keypadToolbar = UIToolbar()
        keypadToolbar.items = [
            UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: self, action: nil),
            UIBarButtonItem(title: "Next", style: .done, target: nextTextField, action: #selector(UITextField.becomeFirstResponder)),
        ]
        keypadToolbar.sizeToFit()
        textField.inputAccessoryView = keypadToolbar
    }
    
    static func addDoneButton(_ textView: UITextView) {
        let keypadToolbar = UIToolbar()
        
        keypadToolbar.items = [
            UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: self, action: nil),
            UIBarButtonItem(title: "Done", style: .done, target: textView, action: #selector(UITextView.resignFirstResponder)),
        ]
        keypadToolbar.sizeToFit()
        textView.inputAccessoryView = keypadToolbar
    }
    
    static func addNextButton(_ textView: UITextView, to nextTextView: UITextView) {
        let keypadToolbar = UIToolbar()
        keypadToolbar.items = [
            UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: self, action: nil),
            UIBarButtonItem(title: "Next", style: .done, target: nextTextView, action: #selector(UITextView.becomeFirstResponder)),
        ]
        keypadToolbar.sizeToFit()
        textView.inputAccessoryView = keypadToolbar
    }
    
    static func addNextButton(_ textView: UITextView, to nextTextField: UITextField) {
        let keypadToolbar = UIToolbar()
        keypadToolbar.items = [
            UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: self, action: nil),
            UIBarButtonItem(title: "Next", style: .done, target: nextTextField, action: #selector(UITextField.becomeFirstResponder)),
        ]
        keypadToolbar.sizeToFit()
        textView.inputAccessoryView = keypadToolbar
    }
    
    static func callPhone(_ strPhone: String) {
        let formatedNumber = strPhone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined(separator: "")
        if let url = URL(string: "tel://\(formatedNumber)") {
            if #available(iOS 10.0, *) {
                UIApplication.shared.open(url, options: [:], completionHandler: nil)
            } else {
                UIApplication.shared.openURL(url)
            }
        }
    }
    
    static func removeAllSubviews(_ view: UIView) {
        for item in view.subviews {
            item.removeFromSuperview()
        }
    }
    
    static func mimeType(string: String) -> String {
        var result = ""
        
        switch string {
        case "jpeg":
            result = "image/jpeg"
            break
        case "jpg":
            result = "image/jpeg"
            break
        case "png":
            result = "image/png"
            break
        case "gif":
            result = "image/gif"
            break
        case "pdf":
            result = "application/pdf"
            break
        case "xls":
            result = "application/xls"
            break
        case "xlsx":
            result = "application/xls"
            break
        case "doc":
            result = "application/doc"
            break
        case "hwp":
            result = "application/hwp"
            break
        default:
            break
        }
        
        return result
    }
    
    //////////////////////////////////////////////////////////////////////
    // MARK: - Animation
    //////////////////////////////////////////////////////////////////////
    
    static func pushFromRight(_ view: UIView) {
        view.layer.removeAllAnimations()
        let transaction = CATransition()
        transaction.duration = 0.4
        transaction.timingFunction = CAMediaTimingFunction(name: CAMediaTimingFunctionName.easeOut)
        transaction.fillMode = CAMediaTimingFillMode.forwards
        transaction.type = CATransitionType.push
        transaction.subtype = CATransitionSubtype.fromRight
        view.layer.add(transaction, forKey: "pushFromRight")
    }
    
    static func pushFromLeft(_ view: UIView) {
        view.layer.removeAllAnimations()
        let transaction = CATransition()
        transaction.duration = 0.4
        transaction.timingFunction = CAMediaTimingFunction(name: CAMediaTimingFunctionName.easeOut)
        transaction.fillMode = CAMediaTimingFillMode.forwards
        transaction.type = CATransitionType.push
        transaction.subtype = CATransitionSubtype.fromLeft
        view.layer.add(transaction, forKey: "pushFromLeft")
    }
    
    static func animationFade(_ view: UIView) {
        view.layer.removeAllAnimations()
        let transaction = CATransition()
        transaction.duration = 0.4
        transaction.timingFunction = CAMediaTimingFunction(name: CAMediaTimingFunctionName.easeOut)
        transaction.fillMode = CAMediaTimingFillMode.forwards
        transaction.type = CATransitionType.fade
        view.layer.add(transaction, forKey: "animationFade")
    }
    
    //////////////////////////////////////////////////////////////////////
    // MARK: - load & store value
    //////////////////////////////////////////////////////////////////////
    
    static func bool(_ key: String) -> Bool {
        return UserDefaults.standard.bool(forKey: key)
    }
    
    static func setBool(_ value: Bool, forKey: String) {
        UserDefaults.standard.set(value, forKey: forKey)
        UserDefaults.standard.synchronize()
    }
    
    static func int(_ key: String) -> Int {
        return UserDefaults.standard.integer(forKey: key)
    }
    
    static func setInt(_ value: Int, forKey: String) {
        UserDefaults.standard.set(value, forKey: forKey)
        UserDefaults.standard.synchronize()
    }
    
    static func string(_ key: String) -> String! {
        return UserDefaults.standard.string(forKey: key)
    }
    
    static func setString(_ value: String!, forKey: String) {
        UserDefaults.standard.set(value, forKey: forKey)
        UserDefaults.standard.synchronize()
    }
    
    static func data(_ key: String) -> Any! {
        return UserDefaults.standard.object(forKey: key) as Any
    }
    
    static func setData(_ value: Any!, forKey: String) {
        UserDefaults.standard.set(value, forKey: forKey)
        UserDefaults.standard.synchronize()
    }
    
    static func archivedData(_ key: String) -> Any! {
        if let data = UserDefaults.standard.object(forKey: key) as? NSData {
            return NSKeyedUnarchiver.unarchiveObject(with: data as Data) as AnyObject
        }
        return nil
    }
    
    static func setArchivedData(_ value: Any!, forKey: String) {
        let data = NSKeyedArchiver.archivedData(withRootObject: value)
        UserDefaults.standard.set(data, forKey: forKey)
        UserDefaults.standard.synchronize()
    }
    
    static func sizePerKB(url: String) -> Double {
        let _url = URL(string: url)
        guard let filePath = _url?.path else {
            return 0
        }
        do {
            let attribute = try FileManager.default.attributesOfItem(atPath: filePath)
            if let size = attribute[FileAttributeKey.size] as? NSNumber {
                return size.doubleValue / 1024
            }
        } catch {
            print("Error: \(error)")
        }
        return 0
    }
    
    static func getStrHex(hexString: String)->String{
        let hex = hexString.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int = UInt32()
        
        Scanner(string: hex).scanHexInt32(&int)
        
        let a, r, g, b: UInt32
        (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        
        return String(format: "%.2f %.2f %.2f 1.0", Float(r)/255, Float(g)/255, Float(b)/255)
    }
}

extension URL {
    func valueOf(_ queryParamaterName: String) -> String? {
        guard let url = URLComponents(string: self.absoluteString) else { return nil }
        return url.queryItems?.first(where: { $0.name == queryParamaterName })?.value
    }
}

extension Double {
	
	func round(_ digits: Int = 2) -> Double {
		let multiplier = pow(10, Double(digits))
		return Darwin.round(self * multiplier) / multiplier
	}
	
}


extension Float {
	
	func round(_ digits: Int = 2) -> Float {
		let multiplier = pow(10, Float(digits))
		return Darwin.round(self * multiplier) / multiplier
	}
	
	//. 12345.678 with "000.0" format -> 12345.6
	//. 1.2345 with "000.0" format -> 001.2
	func formatString(format: String) -> String {
		let formatter = NumberFormatter()
		formatter.positiveFormat = format
		return formatter.string(from: NSNumber(value: self))!
	}
	
}


extension URL {
    
    public var queryParams: [String: String]? {
        guard
            let components = URLComponents(url: self, resolvingAgainstBaseURL: true),
            let queryItems = components.queryItems else { return nil }
        return queryItems.reduce(into: [String: String]()) { (result, item) in
            result[item.name] = item.value
        }
    }
    
}


extension UIColor {
    convenience init(hexString: String) {
        let hex = hexString.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int = UInt32()
        Scanner(string: hex).scanHexInt32(&int)
        let a, r, g, b: UInt32
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(red: CGFloat(r) / 255, green: CGFloat(g) / 255, blue: CGFloat(b) / 255, alpha: CGFloat(a) / 255)
    }
}

public extension UIImage {

    /**
    Returns image with size 1x1px of certain color.
    */
    class func imageWithColor(color : UIColor) -> UIImage {
        let rect = CGRect.init(x: 0, y: 0, width: 375, height: 375)
        UIGraphicsBeginImageContext(rect.size)
        let context = UIGraphicsGetCurrentContext()

        context!.setFillColor(color.cgColor)
        context!.fill(rect)

        let image = UIGraphicsGetImageFromCurrentImageContext()!
        UIGraphicsEndImageContext()

        return image
    }
}
