import UIKit

class ScanPopup: BaseVC {
    
    @IBOutlet var vwPopup: UIView!
    
    @IBOutlet var lblTitle: UILabel!
    
    @IBOutlet var lblMessage: UITextField!
    @IBOutlet var btnConfirm: UIButton!
    @IBOutlet var checker: UISwitch!

    @IBOutlet var vPopup: UIView!
    
    var mTitle = ""
    var mMsg = ""
    var cbOk: callback!

    public typealias callback = (String, Bool) -> ()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        initUI()
    }
    
    convenience init(_ okCallback: callback! = nil) {
        self.init()
        
        mTitle = "알림"
        cbOk = okCallback
    }
    
    convenience init(_ title: String, _ okCallback: callback! = nil) {
        self.init()
        
        mTitle = title
        cbOk = okCallback
    }
    
    static func show(_ vc: UIViewController, title: String = "2차기 제품 S/N",_ okCallback: callback! = nil) {
        let _vc = ScanPopup(title, okCallback)
        _vc.modalPresentationStyle = .overCurrentContext
        _vc.modalTransitionStyle = .crossDissolve
        vc.present(_vc, animated: true, completion: nil)
    }
    
    //////////////////////////////////////////////////////////////////////
    // MARK: - Helper
    //////////////////////////////////////////////////////////////////////
    
    func initUI() {
//        vPopup.layer.cornerRadius = 10
//        btnConfirm.layer.cornerRadius = 5
        let color = UIColor.init(red: 220/255, green: 220/255, blue: 220/255, alpha: 1)
        vPopup.layer.borderColor = color.cgColor.copy()
        vPopup.layer.borderWidth = 1
        
        lblTitle.text = mTitle
    }
    
    //////////////////////////////////////////////////////////////////////
    // MARK: - Action
    //////////////////////////////////////////////////////////////////////
    @IBAction func onChangeSwitch(_ sender: Any) {
        if checker.isOn {
            lblMessage.text = "확인 불가"
            lblMessage.isEnabled = false
        }else{
            lblMessage.text = ""
            lblMessage.isEnabled = true
        }
    }
    
    @IBAction func onBackgroundTap(_ sender: Any) {
        // removeFromSuperviewWithAnimation()
        onBtnClose()
    }
    
    @IBAction func onBtnClose(_ sender: Any? = nil) {
        dismiss(animated: true, completion: nil)
    }
    
    @IBAction func onBtnOK(_ sender: Any? = nil) {
        dismiss(animated: true, completion: nil)
        if cbOk != nil {
            let str = lblMessage.text
            cbOk(str!, false)
        }
    }
    
}
