extension MediaPlayer {

    func addPlayers(player: MediaPlayerView) {
        players.append(player)
    }

    func removePlayer(playerId: String) {
        guard let player = players.first(where: {$0.playerId == playerId}) else {
            return
        }
        players.remove(at: players.firstIndex(of: player)!)
    }

    func removeAllPlayers() {
        players.removeAll()
    }

    func getPlayer(playerId: String) -> MediaPlayerView? {
        guard let player = players.first(where: {$0.playerId == playerId}) else {
            return nil
        }
        return player
    }
}
