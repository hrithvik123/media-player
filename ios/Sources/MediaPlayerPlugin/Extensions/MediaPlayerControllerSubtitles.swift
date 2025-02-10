import Foundation
import AVKit

extension MediaPlayerController {

    func setSubtitles() {

        if var subTitleUrl = self.extra.subtitles?.url {
            // check if subtitle is .srt
            if subTitleUrl.pathExtension == "srt" {
                let vttUrl: URL = srtSubtitleToVtt(srtURL: subTitleUrl)
                subTitleUrl = vttUrl
            }
            var textStyle: [AVTextStyleRule] = []
            if let opt = self.extra.subtitles?.settings {
                textStyle.append(contentsOf: self.setSubTitleStyle(options: opt))
            }

            let subTitleAsset = AVAsset(url: subTitleUrl)
            let composition = AVMutableComposition()

            if let videoTrack = composition.addMutableTrack(
                withMediaType: AVMediaType.video,
                preferredTrackID: Int32(kCMPersistentTrackID_Invalid)) {
                if let audioTrack = composition.addMutableTrack(
                    withMediaType: AVMediaType.audio,
                    preferredTrackID: Int32(kCMPersistentTrackID_Invalid)) {
                    do {
                        try videoTrack.insertTimeRange(
                            CMTimeRangeMake(start: CMTime.zero,
                                            duration: self.videoAsset.duration),
                            of: self.videoAsset.tracks(
                                withMediaType: AVMediaType.video)[0],
                            at: CMTime.zero)
                        // if video has an audio track
                        if self.videoAsset.tracks.count > 0 {
                            let clipAudioTrack = self.videoAsset.tracks(
                                withMediaType: AVMediaType.audio)[0]
                            try audioTrack.insertTimeRange(CMTimeRangeMake(
                                                            start: CMTime.zero,
                                                            duration: self.videoAsset.duration),
                                                           of: clipAudioTrack, at: CMTime.zero)
                        }
                        // Adds subtitle track
                        if let subtitleTrack = composition.addMutableTrack(
                            withMediaType: .text,
                            preferredTrackID: kCMPersistentTrackID_Invalid) {
                            do {
                                let duration = self.videoAsset.duration
                                try subtitleTrack.insertTimeRange(
                                    CMTimeRangeMake(start: CMTime.zero,
                                                    duration: duration),
                                    of: subTitleAsset.tracks(
                                        withMediaType: .text)[0],
                                    at: CMTime.zero)

                                self.playerItem = AVPlayerItem(asset: composition)
                                self.playerItem.textStyleRules = textStyle
                            } catch {}
                        }
                    } catch {}
                }
            }
        }
    }

    private func setSubTitleStyle(options: MediaPlayerSubtitleSettings) -> [AVTextStyleRule] {
        var styles: [AVTextStyleRule] = []
        var backColor: [Float] = [1.0, 0.0, 0.0, 0.0]
        if let bckCol = options.backgroundColor as? String {
            let color = self.getColorFromRGBA(rgba: bckCol)
            backColor = color.count > 0 ? color : backColor
        }
        if let textStyle: AVTextStyleRule = AVTextStyleRule(textMarkupAttributes: [
            kCMTextMarkupAttribute_CharacterBackgroundColorARGB as String:
                backColor
        ]) {
            styles.append(textStyle)
        }

        var foreColor: [Float] = [1.0, 1.0, 1.0, 1.0]
        if let foreCol = options.foregroundColor as? String {
            let color = self.getColorFromRGBA(rgba: foreCol)
            foreColor = color.count > 0 ? color : foreColor
        }
        if let textStyle1: AVTextStyleRule = AVTextStyleRule(textMarkupAttributes: [
            kCMTextMarkupAttribute_ForegroundColorARGB as String: foreColor
        ]) {
            styles.append(textStyle1)
        }
        var ftSize = 160
        if let pixSize = options.fontSize as? Int {
            ftSize = pixSize * 10
        }
        if let textStyle2: AVTextStyleRule = AVTextStyleRule(textMarkupAttributes: [
            kCMTextMarkupAttribute_RelativeFontSize as String: ftSize,
            kCMTextMarkupAttribute_CharacterEdgeStyle as String: kCMTextMarkupCharacterEdgeStyle_None
        ]) {
            styles.append(textStyle2)
        }
        return styles
    }

    private func getColorFromRGBA(rgba: String) -> [Float] {
        if let oPar = rgba.firstIndex(of: "(") {
            if let cPar = rgba.firstIndex(of: ")") {
                let strColor = rgba[rgba.index(after: oPar)..<cPar]
                let array = strColor.components(separatedBy: ",")
                if array.count == 4 {
                    var retArray: [Float] = []
                    retArray.append((array[3]
                                        .trimmingCharacters(in: .whitespaces) as NSString)
                                        .floatValue)
                    retArray.append((array[0]
                                        .trimmingCharacters(in: .whitespaces) as NSString)
                                        .floatValue / 255)
                    retArray.append((array[1]
                                        .trimmingCharacters(in: .whitespaces) as NSString)
                                        .floatValue / 255)
                    retArray.append((array[2]
                                        .trimmingCharacters(in: .whitespaces) as NSString)
                                        .floatValue / 255)
                    return retArray
                } else {
                    return []
                }
            } else {
                return []
            }
        } else {
            return []
        }
    }

    private func srtSubtitleToVtt(srtURL: URL) -> URL {
        guard let cachesURL = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first else {
            fatalError("Couldn't get caches directory")
        }
        let vttFileName = UUID().uuidString + ".vtt"
        let vttURL = cachesURL.appendingPathComponent(vttFileName)
        let session = URLSession(configuration: .default)
        let vttFolderURL = vttURL.deletingLastPathComponent()
        do {
            try FileManager.default.createDirectory(at: vttFolderURL, withIntermediateDirectories: true, attributes: nil)
        } catch let error {
            print("Creating folder error: ", error)
        }
        let task = session.dataTask(with: srtURL) { (data, _, error) in
            guard let data = data, error == nil else {
                print("Download failed: \(error?.localizedDescription ?? "ukn")")
                return
            }
            do {
                let tempSRTURL = URL(fileURLWithPath: NSTemporaryDirectory()).appendingPathComponent("subtitulos.srt")
                try data.write(to: tempSRTURL)
                let srtContent = try String(contentsOf: tempSRTURL, encoding: .utf8)
                let vttContent = srtContent.replacingOccurrences(of: ",", with: ".")
                let vttString = "WEBVTT\n\n" + vttContent
                try vttString.write(toFile: vttURL.path, atomically: true, encoding: .utf8)
                try FileManager.default.removeItem(at: tempSRTURL)

            } catch let error {
                print("Processing subs error: \(error)")
                exit(1)
            }

        }

        task.resume()
        return vttURL
    }
}
