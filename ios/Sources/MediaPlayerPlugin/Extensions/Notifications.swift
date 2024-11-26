import Foundation
import MediaPlayer

extension NSNotification.Name {
    static var mediaPlayerReady: Notification.Name {return .init(rawValue: "mediaPlayerReady")}
    static var mediaPlayerPlay: Notification.Name {return .init(rawValue: "mediaPlayerPlay")}
    static var mediaPlayerPause: Notification.Name {return .init(rawValue: "mediaPlayerPause")}
    static var mediaPlayerEnded: Notification.Name {return .init(rawValue: "mediaPlayerEnded")}
    static var mediaPlayerRemoved: Notification.Name {return .init(rawValue: "mediaPlayerRemoved")}
    static var mediaPlayerSeeked: Notification.Name {return .init(rawValue: "mediaPlayerSeeked")}
    static var mediaPlayerTimeUpdate: Notification.Name {return .init(rawValue: "mediaPlayerTimeUpdate")}

    static var mediaPlayerFullscreen: Notification.Name {return .init(rawValue: "mediaPlayerFullscreen")}
    static var mediaPlayerPictureInPicture: Notification.Name {return .init(rawValue: "mediaPlayerPictureInPicture")}

    static var mediaPlayerIsPlayingInBackground: Notification.Name { return .init(rawValue: "isPlayingInBackground")}
}

extension MediaPlayerPlugin {

    @objc func mediaPlayerReady(notification: Notification) {
        guard let info = notification.userInfo as? [String: Any] else {return}
        DispatchQueue.main.async {
            self.notifyListeners("MediaPlayer:Ready", data: info)
        }
    }

    @objc func mediaPlayerPlay(notification: Notification) {
        guard let info = notification.userInfo as? [String: Any] else { return }
        DispatchQueue.main.async {
            self.notifyListeners("MediaPlayer:Play", data: info)
        }
    }

    @objc func mediaPlayerPause(notification: Notification) {
        guard let info = notification.userInfo as? [String: Any] else { return }
        DispatchQueue.main.async {
            self.notifyListeners("MediaPlayer:Pause", data: info)

        }
    }

    @objc func mediaPlayerEnded(notification: Notification) {
        guard let info = notification.userInfo as? [String: Any] else { return }
        DispatchQueue.main.async {
            self.notifyListeners("MediaPlayer:Ended", data: info)
        }
    }

    @objc func mediaPlayerRemoved(notification: Notification) {
        guard let info = notification.userInfo as? [String: Any] else { return }
        DispatchQueue.main.async {
            self.notifyListeners("MediaPlayer:Removed", data: info)
        }
    }

    @objc func mediaPlayerSeeked(notification: Notification) {
        guard let info = notification.userInfo as? [String: Any] else { return }
        DispatchQueue.main.async {
            self.notifyListeners("MediaPlayer:Seeked", data: info)
        }
    }

    @objc func mediaPlayerTimeUpdate(notification: Notification) {
        guard let info = notification.userInfo as? [String: Any] else { return }
        DispatchQueue.main.async {
            self.notifyListeners("MediaPlayer:TimeUpdate", data: info)
        }
    }

    @objc func mediaPlayerFullscreen(notification: Notification) {
        guard let info = notification.userInfo as? [String: Any] else { return }
        DispatchQueue.main.async {
            self.notifyListeners("MediaPlayer:Fullscreen", data: info)
        }
    }

    @objc func mediaPlayerPictureInPicture(notification: Notification) {
        guard let info = notification.userInfo as? [String: Any] else { return }
        DispatchQueue.main.async {
            self.notifyListeners("MediaPlayer:PictureInPicture", data: info)
        }
    }
    
    @objc func mediaPlayerIsPlayingInBackground(notification: Notification) {
        guard let info = notification.userInfo as? [String: Any] else { return }
        DispatchQueue.main.async {
            self.notifyListeners("MediaPlayer:isPlayingInBackground", data: info)
        }
    }
    
}
