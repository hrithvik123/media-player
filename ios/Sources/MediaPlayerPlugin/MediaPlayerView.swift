import UIKit
import AVKit
import MediaPlayer

open class MediaPlayerView: UIView {
    var playerId: String
    var url: URL

    var ios: MediaPlayerIosOptions
    var extra: MediaPlayerExtraOptions

    var isPIPModeAvailable: Bool = false
    var isInBackgroundMode: Bool = false
    var isInPipMode: Bool = false
    var isFullscreen: Bool = false
    
    var currentTime: Double = 0
    var duration: Double = 0
    var rate: Float = 1.0

    var isLoaded: Bool = false
    var isVideoEnd: Bool = false
    var isRateZero: Bool = false
    
    var isPlayingObserver: NSKeyValueObservation?
    var isReadyObserver: NSKeyValueObservation?
    var rateObserver: NSKeyValueObservation?
    var timeObserver: NSKeyValueObservation?
    var seekObserver: NSKeyValueObservation?

    var screenRotationObserver: Any?
    var backgroundObserver: Any?
    var foregroundObserver: Any?

    var periodicTimeObserver: Any?
    
    var player: AVPlayer?
    var playerFrame: CGRect
    var videoPlayer: AVPlayerViewController
    var videoAsset: AVURLAsset
    var playerItem: AVPlayerItem?
    var audioSession: AVAudioSession?

    init(playerId: String, url: URL, ios: MediaPlayerIosOptions, extra: MediaPlayerExtraOptions) {
        self.playerId = playerId
        self.url = url
        self.ios = ios
        self.extra = extra
        
        playerFrame = CGRect(
            x: ios.left,
            y: ios.top,
            width: ios.width,
            height: ios.height
        )
        
        videoPlayer = AVPlayerViewController()

        if(self.extra.headers != nil){
            videoAsset = AVURLAsset(url: url, options: ["AVURLAssetHTTPHeaderFieldsKey": self.extra.headers])
        } else {
            videoAsset = AVURLAsset(url: url)
        }
        
        super.init(frame: playerFrame)
        self.clipsToBounds = true
        self.contentMode = .scaleAspectFit
        videoPlayer.delegate = self
        videoPlayer.updatesNowPlayingInfoCenter = false
        videoPlayer.view.frame = self.bounds

        if(self.extra.subtitles != nil) {
            setSubtitles()
        } else {
            self.playerItem = AVPlayerItem(asset: self.videoAsset)
        }
        player = AVPlayer(playerItem: playerItem)
        
        if #available (iOS 16.0, *) {
            videoPlayer.allowsVideoFrameAnalysis = ios.allowsVideoFrameAnalysis
        }
        
        videoPlayer.showsPlaybackControls = self.extra.showControls == true

        if (UIDevice.current.userInterfaceIdiom == UIUserInterfaceIdiom.pad),
           #available(iOS 13.0, *) {
            isPIPModeAvailable = true
        } else if #available(iOS 14.0, *) {
            isPIPModeAvailable = true
        }
        if #available(iOS 14.2, *) {
            if(self.ios.automaticallyEnterPiP == true) {
                videoPlayer.canStartPictureInPictureAutomaticallyFromInline = true
            } else {
                videoPlayer.canStartPictureInPictureAutomaticallyFromInline = false
            }
        }
        videoPlayer.allowsPictureInPicturePlayback = (isPIPModeAvailable && self.ios.enablePiP == true)
        videoPlayer.player = player

        self.addObservers()
        
        if self.ios.openInFullscreen == true {
            videoPlayer.enterFullScreen(animated: true)
        }
        
        DispatchQueue.main.async {
            self.audioSession = AVAudioSession.sharedInstance()
            do {
                try self.audioSession?.setCategory(.playback, mode: .moviePlayback)
                try self.audioSession?.setActive(true)
                print("Audio session activated")
            } catch let error as NSError {
                print("Unable to activate audio session:  \(error.localizedDescription)")
                print(error)
            }
        }
        
        self.addSubview(videoPlayer.view)
    }
    
    public func releasePlayer() {
        self.removeObservers()
        self.player?.replaceCurrentItem(with: nil)
        self.videoPlayer.view.removeFromSuperview()
        self.videoPlayer.removeFromParent()
        self.removeFromSuperview()
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
