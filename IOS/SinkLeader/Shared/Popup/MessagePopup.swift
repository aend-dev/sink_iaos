import UIKit

class MessagePopup: BaseVC {
    
    @IBOutlet var vwPopup: UIView!
    
    @IBOutlet var lblMessage: UILabel!
    @IBOutlet var btnConfirm: UIButton!

    @IBOutlet var vPopup: UIView!

    var mMsg = ""
    var cbOk: callback!

    public typealias callback = () -> ()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        initUI()
    }
    
    convenience init(_ message: String, _ okCallback: callback! = nil) {
        self.init()
        
        mMsg = message
        cbOk = okCallback
    }
    
    static func show(_ vc: UIViewController, _ message: String,_ okCallback: callback! = nil) {
        let _vc = MessagePopup(message, okCallback)
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
//        let color = UIColor.init(red: 0, green: 164/255, blue: 225/255, alpha: 1)
//        btnConfirm.layer.borderColor = color.cgColor.copy()
//        btnConfirm.layer.borderWidth = 2
        
//        lblTitle.text = mTitle
        lblMessage.text = mMsg
    }
    
    //////////////////////////////////////////////////////////////////////
    // MARK: - Action
    //////////////////////////////////////////////////////////////////////
    
    @IBAction func onBackgroundTap(_ sender: Any) {
        // removeFromSuperviewWithAnimation()
        onBtnClose()
    }
    
    @IBAction func onBtnClose(_ sender: Any? = nil) {
        dismiss(animated: true, completion: nil)
        if cbOk != nil {
            cbOk()
        }
    }
    
}
