import Foundation
import AudioToolbox
import AVFoundation

/// System tone and notification chime player.
/// Equivalent to Android's ToneGenerator(AudioManager.STREAM_NOTIFICATION) & RingtoneManager.
public class AudioChimePlayer {
    public static let shared = AudioChimePlayer()

    public func playNotificationSound() {
        // System Sound ID 1007: Standard iOS notification chime
        AudioServicesPlaySystemSound(1007)
    }

    public func playTaskCompletionSound() {
        // System Sound ID 1025: Cheerful completion chime
        AudioServicesPlaySystemSound(1025)
    }

    public func playUrgentAlertSound() {
        // System Sound ID 1005: High priority warning chime + Haptic vibration
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
        AudioServicesPlaySystemSound(1005)
    }
}
