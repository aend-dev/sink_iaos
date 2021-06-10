import UIKit

class DialogPopup2: BaseVC {
    
    @IBOutlet var vwPopup: UIView!
    
    @IBOutlet var lblTitle: UILabel!
    
    @IBOutlet var btnConfirm: UIButton!

    @IBOutlet var vPopup: UIView!
    

    var mTitle = ""
    var cbOk: callback!
    var cbCencel: callback!

    public typealias callback = () -> ()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        initUI()
    }
    
    convenience init(_ title: String, _ cbOk: callback! = nil, _ cbCencel: callback! = nil) {
        self.init()
        
        mTitle = title
        self.cbOk = cbOk
        self.cbCencel = cbCencel
    }
    
    static func show(_ vc: UIViewController, _ message: String,_ cbOk: callback! = nil, _ cbCencel: callback! = nil) {
        let _vc = DialogPopup2(message, cbOk, cbCencel)
        _vc.modalPresentationStyle = .overCurrentContext
        _vc.modalTransitionStyle = .crossDissolve
        vc.present(_vc, animated: true, completion: nil)
    }
    
    //////////////////////////////////////////////////////////////////////
    // MARK: - Helper
    //////////////////////////////////////////////////////////////////////
    
    func initUI() {        
        lblTitle.text = mTitle
    }
    
    //////////////////////////////////////////////////////////////////////
    // MARK: - Action
    //////////////////////////////////////////////////////////////////////
    
    @IBAction func onBackgroundTap(_ sender: Any) {
        // removeFromSuperviewWithAnimation()
        onBtnClose()
    }
    
    @IBAction func onBtnOK(_ sender: Any? = nil) {
        dismiss(animated: true, completion: nil)
        if cbOk != nil {
            cbOk()
        }
    }
    
    @IBAction func onBtnClose(_ sender: Any? = nil) {
        dismiss(animated: true, completion: nil)
        if cbCencel != nil {
            cbCencel()
        }
    }
    
}
