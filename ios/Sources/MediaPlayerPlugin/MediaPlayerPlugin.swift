import Foundation
import Capacitor
import UIKit
import AVKit

@objc(MediaPlayerPlugin)
public class MediaPlayerPlugin: CAPPlugin, CAPBridgedPlugin {
    
    public let identifier = "MediaPlayerPlugin"
    public let jsName = "MediaPlayer"

    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "create", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "play", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "pause", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getDuration", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getCurrentTime", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setCurrentTime", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isPlaying", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isMuted", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setVisibilityBackgroundForPiP", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "mute", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getVolume", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setVolume", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getRate", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setRate", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "remove", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "removeAll", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = MediaPlayer()

    var mediaPlayerPlayObserver: Any?;
    var mediaPlayerPauseObserver: Any?;
    var mediaPlayerEndedObserver: Any?;
    var mediaPlayerReadyObserver: Any?;
    var mediaPlayerSeekObserver: Any?
    var mediaPlayerTimeUpdatedObserver: Any?
    var mediaPlayerFullscreenObserver: Any?
    var mediaPlayerPictureInPictureObserver: Any?
    var mediaPlayerIsPlayingInBackgroundObserver: Any?

    override public func load() {
        addNotificationCenterObservers();
        implementation.setBridge(bridge: self.bridge!)
    }

    deinit {
        removeNotificationCenterObservers();
    }

    @objc func create(_ call: CAPPluginCall) {
        guard let playerId = call.options["playerId"] as? String else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "play", "message": error]);
            return
        }
        guard let url = call.options["url"] as? String else {
            let error: String = "Must provide a url"
            print(error);
            call.resolve(["result": false, "method": "play", "message": error]);
            return
        }
        
        let dictUrl: [String: Any] = getURLFromFilePath(filePath: url)
        if let message = dictUrl["message"] as? String {
            if message.count > 0 {
                call.resolve([ "result": false, "method": "create", "message": message])
                return
            }
        }
        guard let parsedUrl = dictUrl["url"] as? URL else {
            call.resolve([ "result": false, "method": "create", "message": "URL not defined"])
            return
        }


        let placementOptions = call.options["placement"] as? [String: Any] ?? [:]
        let iosOptions = call.options["ios"] as? [String: Any] ?? [:]
        let extraOptions = call.options["extra"] as? [String: Any] ?? [:]
        let subtitleOptions = extraOptions["subtitles"] as? [String: Any] ?? nil
        
        let ios = MediaPlayerIosOptions(
            enableExternalPlayback: iosOptions["enableExternalPlayback"] as? Bool, enablePiP: iosOptions["enablePiP"] as? Bool, enableBackgroundPlay: iosOptions["enableBackgroundPlay"] as? Bool, openInFullscreen: iosOptions["openInFullscreen"] as? Bool, automaticallyEnterPiP: iosOptions["automaticallyEnterPiP"] as? Bool, automaticallyHideBackgroundForPip: iosOptions["automaticallyHideBackgroundForPip"] as? Bool, fullscreenOnLandscape: iosOptions["fullscreenOnLandscape"] as? Bool, allowsVideoFrameAnalysis: iosOptions["allowsVideoFrameAnalysis"] as? Bool
        )
                        
        var subTitle: URL?
        if subtitleOptions != nil {
            let dictSubTitle: [String: Any] = getURLFromFilePath(filePath: subtitleOptions!["url"] as! String)
            if let message = dictSubTitle["message"] as? String {
                if message.count > 0 {
                    call.resolve([ "result": false, "method": "create",
                                   "message": message])
                    return
                }
            }
            guard let sturl = dictSubTitle["url"] as? URL else {
                call.resolve([ "result": false, "method": "create",
                               "message": "Subtitles URL not defined"])
                return
            }
            subTitle = sturl
        }
        
        let subtitles = (subtitleOptions != nil) ? MediaPlayerSubtitleOptions(
            url: subTitle!, language: subtitleOptions?["language"] as? String, foregroundColor: subtitleOptions!["foregroundColor"] as? String, backgroundColor: subtitleOptions!["backgroundColor"] as? String, fontSize:subtitleOptions?["fontSize"] as? Float
        ) : nil

        var posterURL: URL?
        if let poster = extraOptions["poster"] as? String {
            let posterDictUrl: [String: Any] = getURLFromFilePath(filePath: poster)
            if let parsedUrl = posterDictUrl["url"] as? URL {
                posterURL = parsedUrl
            }
        }
        
        let extra = MediaPlayerExtraOptions(
            title: extraOptions["title"] as? String, subtitle: extraOptions["subtitle"] as? String, poster: posterURL, artist: extraOptions["artist"] as? String, rate: extraOptions["rate"] as? Float, subtitles: subtitles, autoPlayWhenReady: extraOptions["autoPlayWhenReady"] as? Bool, loopOnEnd: extraOptions["loopOnEnd"] as? Bool, showControls:extraOptions["showControls"] as? Bool, headers: extraOptions["headers"] as? [String: String]
        )

        let placement = MediaPlayerPlacementOptions(
            height: placementOptions["height"] as? Float, width: placementOptions["width"] as? Float, videoOrientation: placementOptions["videoOrientation"] as? String,
            verticalMargin: placementOptions["verticalMargin"] as? Float, horizontalMargin: placementOptions["horizontalMargin"] as? Float, horizontalAlignment: placementOptions["horizontalAlignment"] as? String, verticalAlignment: placementOptions["verticalAlignment"] as? String
        )
        
        implementation.create(call: call, playerId: playerId, url: parsedUrl, placement: placement, ios: ios, extra: extra)
    }

    @objc func play(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "play", "message": error]);
            return
        }
        implementation.play(call: call, playerId: playerId)
    }

    @objc func pause(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "pause", "message": error]);
            return
        }
        implementation.pause(call: call, playerId: playerId)
    }

    @objc func getDuration(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "getDuration", "message": error]);
            return
        }
        implementation.getDuration(call: call, playerId: playerId)
    }

    @objc func getCurrentTime(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "getCurrentTime", "message": error]);
            return
        }
        implementation.getCurrentTime(call: call, playerId: playerId);
    }

    @objc func setCurrentTime(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "setCurrentTime", "message": error]);
            return
        }
        guard let time = call.getDouble("time") else {
            let error: String = "Must provide a time"
            print(error);
            call.resolve(["result": false, "method": "setCurrentTime", "message": error]);
            return
        }
        implementation.setCurrentTime(call: call, playerId: playerId, time: time);
    }

    @objc func isPlaying(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "isPlaying", "message": error]);
            return
        }
        implementation.isPlaying(call: call, playerId: playerId);
    }

    @objc func isMuted(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "isMuted", "message": error]);
            return
        }
        implementation.isMuted(call: call, playerId: playerId)
    }
    
    @objc func setVisibilityBackgroundForPiP(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "setVisibilityBackgroundForPiP", "message": error]);
            return
        }
        guard let isVisible = call.getBool("isVisible") else {
            let error: String = "Must provide isVisible"
            print(error);
            call.resolve(["result": false, "method": "setVisibilityBackgroundForPiP", "message": error]);
            return
        }
        implementation.setVisibilityBackgroundForPiP(call: call, playerId: playerId, isVisible: isVisible)
    }

    @objc func mute(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "mute", "message": error]);
            return
        }
        implementation.mute(call: call, playerId: playerId)
    }

    @objc func getVolume(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "getVolume", "message": error]);
            return
        }
        implementation.getVolume(call: call, playerId: playerId)
    }

    @objc func setVolume(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "setVolume", "message": error]);
            return
        }
        guard let volume = call.getFloat("volume") else {
            let error: String = "Must provide a volume"
            print(error);
            call.resolve(["result": false, "method": "setVolume", "message": error]);
            return
        }
        implementation.setVolume(call: call, playerId: playerId, volume: volume)
    }

    @objc func getRate(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "getRate", "message": error]);
            return
        }
        implementation.getRate(call: call, playerId: playerId)
    }

    @objc func setRate(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "setRate", "message": error]);
            return
        }
        guard let rate = call.getFloat("rate") else {
            let error: String = "Must provide a rate"
            print(error);
            call.resolve(["result": false, "method": "setRate", "message": error]);
            return
        }
        implementation.setRate(call: call, playerId: playerId, rate: rate)
    }

    @objc func remove(_ call: CAPPluginCall) {
        guard let playerId = call.getString("playerId") else {
            let error: String = "Must provide a playerId"
            print(error);
            call.resolve(["result": false, "method": "remove", "message": error]);
            return
        }
        implementation.remove(call: call, playerId: playerId)
    }

    @objc func removeAll(_ call: CAPPluginCall) {
        implementation.removeAll(call: call)
    }

}
