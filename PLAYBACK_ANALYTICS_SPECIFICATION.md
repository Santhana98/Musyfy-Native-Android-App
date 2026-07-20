# Playback Analytics Specification (Musyfy)

This document establishes the official Playback Analytics Taxonomy and specification for Musyfy. It serves as the long-term contract for all future analytics milestones. All developers must adhere to the design principles, ownership mappings, state triggers, and deduplication rules defined herein.

---

## 1. Introduction & Design Principles

### Purpose of the Playback Analytics Architecture
The playback analytics subsystem in Musyfy is designed to capture high-fidelity, accurate user behavior metrics and player performance statistics. These metrics are crucial for:
- Understanding user engagement (listening habits, skipping behavior, and session duration).
- Assessing audio delivery quality (buffering, error rates).
- Informing content recommendations and royalty calculations based on actual listening completeness.

### Architectural Alignment
The specification is built around Musyfy’s core architectural principles:
* **Native Android & Kotlin**: The specification is customized for the Android platform and matches modern Kotlin paradigms.
* **MVVM & Clean Architecture**: Logic is clean, decoupled, and respects layer boundaries.
* **Repository Pattern**: Data and remote/local synchronization remain separated from analytics triggers.
* **Hilt Dependency Injection**: The analytics trackers are injectable, singleton objects conforming to Dagger-Hilt standards.
* **Single Source of Truth**: Only one component is responsible for detecting and reporting any given state change.

### User Intent vs. Actual Playback Outcome
A critical rule of this taxonomy is the distinction between **User Intent** and **Actual Playback Outcome**. 
- **User Intent**: The user’s request to perform an action (e.g., clicking the "Play" button, clicking "Next", dragging the seek slider). These actions are captured at the UI/ViewModel layer. They represent *what the user wants to happen*.
- **Actual Playback Outcome**: The actual state change that occurred in the underlying player engine (ExoPlayer). For example, playback starting after buffering, transitioning to paused state when the audio track halts, or naturally completing. These events are captured in the playback service layer (`Player.Listener`). They represent *what actually happened*.

```
+------------------------------------+
|          UI / View Layer           |
|      (Tapping Play, Skip)          |
+------------------------------------+
                  │
                  ▼  (User Intent)
+------------------------------------+
|          PlayerViewModel           |
|       (Methods: play, pause)       |
+------------------------------------+
                  │
                  ▼  (Commands)
+------------------------------------+
|            PlayerManager           |
|     (ExoPlayer Player.Listener)    |
+------------------------------------+
                  │
                  ▼  (Actual Playback Outcome)
+------------------------------------+
|      Playback Analytics Manager    |
|       (Logs to Firebase)           |
+------------------------------------+
```

Analytics events associated with the playback session lifecycle (e.g., `song_play`, `song_pause`, `song_complete`) **must be designed around actual playback lifecycle outcomes** rather than UI button presses. This prevents reporting playback events for failed requests, network timeouts, or instances where audio fails to start.

---

## 2. Playback Events

The following table lists the official playback events. Detailed specifications for each event follow the table.

| Event Name | Firebase Event | Owner | Trigger |
|:---|:---|:---|:---|
| `song_play` | `song_play` | `PlayerManager` | When playback transitions to `PlayerState.PLAYING` from a non-playing state at the beginning of a track. |
| `song_pause` | `song_pause` | `PlayerManager` | When playback transitions from `PlayerState.PLAYING` to `PlayerState.PAUSED`. |
| `song_resume` | `song_resume` | `PlayerManager` | When playback transitions from `PlayerState.PAUSED` to `PlayerState.PLAYING` for the same song. |
| `song_complete` | `song_complete` | `PlayerManager` | When playback naturally reaches the end of the song (`Player.STATE_ENDED`). |
| `song_skip_next` | `song_skip_next` | `PlayerManager` | When playback transitions to the next track in the queue. |
| `song_skip_previous` | `song_skip_previous` | `PlayerManager` | When playback transitions to the previous track in the queue or restarts the current track. |
| `shuffle_toggle` | `shuffle_toggle` | `PlayerManager` | When ExoPlayer's shuffle mode is enabled or disabled. |
| `repeat_toggle` | `repeat_toggle` | `PlayerManager` | When ExoPlayer's repeat mode transitions to a new setting. |

---

### song_play
- **Purpose**: Tracks the initial start of a song's playback. It indicates that the player successfully loaded the media and began rendering audio.
- **Trigger**: ExoPlayer transitions to `isPlaying = true` and `playbackState = Player.STATE_READY`, where the current position `currentPositionMs` is near the beginning of the song (e.g., $< 1000$ ms) and it is a newly transitioned media item.
- **Event Owner**: `PlayerManager` (ExoPlayer `Player.Listener`).
- **Firebase Event Name**: `song_play`
- **Required Parameters**:
  - `song_id`
  - `song_title`
  - `artist`
  - `duration_ms`
  - `source`
  - `playback_mode`
- **Optional Parameters**:
  - `album`

### song_pause
- **Purpose**: Tracks when playback is paused.
- **Trigger**: ExoPlayer transitions to `isPlaying = false` and `playbackState = Player.STATE_READY` when the previous state was `PlayerState.PLAYING`.
- **Event Owner**: `PlayerManager` (ExoPlayer `Player.Listener`).
- **Firebase Event Name**: `song_pause`
- **Required Parameters**:
  - `song_id`
  - `song_title`
  - `artist`
  - `duration_ms`
  - `current_position_ms`
  - `listened_duration_ms`
  - `completion_percentage`
  - `playback_mode`
- **Optional Parameters**:
  - `album`

### song_resume
- **Purpose**: Tracks when a song resumes playback from a paused state.
- **Trigger**: ExoPlayer transitions to `isPlaying = true` and `playbackState = Player.STATE_READY` when the previous state was `PlayerState.PAUSED` and the track ID remains the same.
- **Event Owner**: `PlayerManager` (ExoPlayer `Player.Listener`).
- **Firebase Event Name**: `song_resume`
- **Required Parameters**:
  - `song_id`
  - `song_title`
  - `artist`
  - `duration_ms`
  - `current_position_ms`
  - `playback_mode`
- **Optional Parameters**:
  - `album`

### song_complete
- **Purpose**: Tracks when a song plays all the way to its end.
- **Trigger**: ExoPlayer transitions to `playbackState = Player.STATE_ENDED` (automatically mapped to `PlayerState.COMPLETED`).
- **Event Owner**: `PlayerManager` (ExoPlayer `Player.Listener`).
- **Firebase Event Name**: `song_complete`
- **Required Parameters**:
  - `song_id`
  - `song_title`
  - `artist`
  - `duration_ms`
  - `listened_duration_ms`
  - `completion_percentage`
- **Optional Parameters**:
  - `album`

### song_skip_next
- **Purpose**: Tracks when a transition to the next song occurs. This covers both user-initiated skips and automatic transitions when a song completes.
- **Trigger**: ExoPlayer transitions to a new media item (`onMediaItemTransition`), where the new track index is higher than the previous track index in the active queue.
- **Event Owner**: `PlayerManager` (ExoPlayer `Player.Listener`).
- **Firebase Event Name**: `song_skip_next`
- **Required Parameters**:
  - `song_id`
  - `song_title`
  - `previous_song_id`
  - `previous_song_title`
  - `skip_type` (Values: `"manual"` if triggered by user seeking/action, `"auto"` if triggered by queue progression)
  - `listened_duration_ms` (Of the skipped song)
  - `completion_percentage` (Of the skipped song)
- **Optional Parameters**:
  - `album`

### song_skip_previous
- **Purpose**: Tracks when a transition to the previous song occurs or when the current song is restarted via skip actions.
- **Trigger**: ExoPlayer transitions to a new media item (`onMediaItemTransition`), where the new track index is lower than the previous track index (or when the current track is seeked back to index 0 via the previous control).
- **Event Owner**: `PlayerManager` (ExoPlayer `Player.Listener`).
- **Firebase Event Name**: `song_skip_previous`
- **Required Parameters**:
  - `song_id`
  - `song_title`
  - `previous_song_id`
  - `previous_song_title`
  - `skip_type` (Values: `"manual"`)
  - `listened_duration_ms` (Of the skipped song)
  - `completion_percentage` (Of the skipped song)
- **Optional Parameters**:
  - `album`

### shuffle_toggle
- **Purpose**: Tracks when the user switches shuffle mode on or off.
- **Trigger**: ExoPlayer transitions shuffle mode state (`onShuffleModeEnabledChanged`).
- **Event Owner**: `PlayerManager` (ExoPlayer `Player.Listener`).
- **Firebase Event Name**: `shuffle_toggle`
- **Required Parameters**:
  - `shuffle_enabled` (Values: `true`, `false`)
  - `source`
- **Optional Parameters**:
  - None

### repeat_toggle
- **Purpose**: Tracks when the repeat mode configuration is changed.
- **Trigger**: ExoPlayer transitions repeat mode state (`onRepeatModeChanged`).
- **Event Owner**: `PlayerManager` (ExoPlayer `Player.Listener`).
- **Firebase Event Name**: `repeat_toggle`
- **Required Parameters**:
  - `repeat_mode` (Values: `"off"`, `"one"`, `"all"`)
  - `source`
- **Optional Parameters**:
  - None

---

## 3. Event Parameters

All parameters must conform to clean naming standards and reuse definitions across events where possible.

### Parameter Definitions

| Parameter Name | Data Type | Description | Classification |
|:---|:---|:---|:---|
| `song_id` | String | Unique identifier of the song. | **Required** |
| `song_title` | String | The title of the song. | **Required** |
| `artist` | String | The main artist of the song. | **Required** |
| `album` | String | The album name the song belongs to. | **Optional** |
| `duration_ms` | Long | Total duration of the song in milliseconds. | **Required** |
| `current_position_ms` | Long | The current playback position in milliseconds. | **Required** |
| `listened_duration_ms` | Long | The cumulative milliseconds the user listened to the song during this playing segment (excluding pauses, seeks, or buffering times). | **Required** |
| `completion_percentage` | Double | Percentage of the song listened to: `(listened_duration_ms / duration_ms) * 100`. | **Required** |
| `source` | String | The origin context that initiated playback (e.g. `"home_feed"`, `"search_results"`, `"liked_songs"`, `"queue_viewer"`, `"deep_link"`). | **Required** |
| `playback_mode` | String | The player playback status combination: `"standard"`, `"shuffle"`, `"repeat_one"`, `"repeat_all"`. | **Required** |
| `previous_song_id` | String | The ID of the song playing immediately before a skip transition occurred. | **Required** (for skip events) |
| `previous_song_title` | String| The title of the song playing immediately before a skip transition occurred. | **Required** (for skip events) |
| `skip_type` | String | How the skip occurred: `"manual"` (user pressed button) or `"auto"` (track ended and next loaded automatically). | **Required** (for skip events) |
| `shuffle_enabled` | Boolean | True if shuffle is active, false otherwise. | **Required** (for shuffle event) |
| `repeat_mode` | String | String mapping of the repeat mode: `"off"`, `"one"`, `"all"`. | **Required** (for repeat event) |
| `connection_type` | String | Connection status when the event occurred: `"wifi"`, `"cellular"`, `"offline"`. | **Future Reserved** |
| `output_device` | String | The audio output target: `"phone_speaker"`, `"bluetooth_headphones"`, `"chromecast"`, `"wired"`. | **Future Reserved** |
| `bitrate_kbps` | Int | Stream bitrate of the playing audio file. | **Future Reserved** |

---

## 4. Event Ownership

To maintain a **Single Source of Truth** and prevent duplicated logging or state discrepancy bugs, each event is strictly mapped to exactly **one owner**.

```
+───────────────────────+─────────────────────────+───────────────────────────────+
| Event Category        | Owner                   | Rationale                     |
+───────────────────────+─────────────────────────+───────────────────────────────+
| Playback Outcomes     | PlayerManager           | ExoPlayer's Player.Listener   |
| (play, pause, skip,   | (ExoPlayer Listener)    | is the only source that knows |
| complete, mode change)|                         | if audio actually rendered.   |
+───────────────────────+─────────────────────────+───────────────────────────────+
| User Interactions     | UI / PlayerViewModel    | Captures the user's intent    |
| (clicks, seeks,       |                         | before player state changes,   |
| button presses)       |                         | enabling delta analysis.      |
+───────────────────────+─────────────────────────+───────────────────────────────+
```

### Rationale & Preserving Single Source of Truth
1. **PlayerManager (ExoPlayer Listener) Ownership**:
   All playback outcomes (`song_play`, `song_pause`, `song_resume`, `song_complete`, `song_skip_next`, `song_skip_previous`, `shuffle_toggle`, `repeat_toggle`) must originate exclusively from the `Player.Listener` implementation inside `PlayerManagerImpl`. 
   - *Why*: The UI or ViewModel cannot accurately determine if a play request succeeds. Factors such as file loading delays, network failures, audio focus losses, or buffering issues might block playback. Triggers placed in the UI/ViewModel will create false-positive events.
2. **UI / PlayerViewModel Ownership**:
   Interaction intents (e.g., `ui_play_click`, `ui_skip_next_click`) are owned by the UI/ViewModel layer. They are triggered immediately when a tap event occurs. By comparing `ui_play_click` (UI) with `song_play` (PlayerManager), product analytics can calculate metrics like *Play Latency* or *Failure-to-Start Rate*.

---

## 5. Trigger Rules

Triggers must be tied to deterministic state changes of ExoPlayer rather than UI events.

```
       Restoring State
       (No Analytics)
              │
              ▼
    ┌───────────────────┐
    │     STATE_IDLE    │
    └─────────┬─────────┘
              │
              ▼  (Media Item Loaded)
    ┌───────────────────┐
    │  STATE_BUFFERING  │
    └─────────┬─────────┘
              │
              ▼  (Audio Starts Rendering)
    ┌───────────────────┐  song_play   ┌───────────────────┐
    │   STATE_READY     ├─────────────►│   PlayerState.    │
    │  (isPlaying=true) │              │      PLAYING      │
    └─────────┬─────────┘              └─────────┬─────────┘
              │                                  │
              │ onMediaItemTransition            │ isPlaying = false
              ▼ (Skip Event)                     ▼ (Pause Event)
    ┌───────────────────┐              ┌───────────────────┐
    │   song_skip_next  │              │    song_pause     │
    │ song_skip_previous│              │PlayerState.PAUSED │
    └───────────────────┘              └─────────┬─────────┘
              ▲                                  │
              │                                  │ isPlaying = true
              │                                  ▼ (Resume Event)
              │                        ┌───────────────────┐
              │                        │    song_resume    │
              │                        └───────────────────┘
              │
              ▼ ExoPlayer STATE_ENDED
    ┌───────────────────┐
    │   song_complete   │
    │    (STATE_ENDED)  │
    └───────────────────┘
```

### Trigger Definitions
* **`song_play`**:
  Fires when `isPlaying` becomes `true`, `playbackState` is `Player.STATE_READY`, AND the current position `currentPositionMs` is $< 1000$ ms. It must only fire once per new song loaded.
* **`song_pause`**:
  Fires when `isPlaying` transitions from `true` to `false`, `playbackState` is `Player.STATE_READY`, and the target state is `PlayerState.PAUSED`. It must not trigger if the player paused due to a song completion, error, or loading state.
* **`song_resume`**:
  Fires when `isPlaying` transitions from `false` to `true` on the same track, and the current position is $\ge 1000$ ms.
* **`song_complete`**:
  Fires if and only if ExoPlayer invokes `onPlaybackStateChanged` with `Player.STATE_ENDED` (natural completion of the track).
* **Skip transitions**:
  Skipping to next/previous transitions (via manual user request or automated queue progression) triggers `song_skip_next` or `song_skip_previous` via `onMediaItemTransition`. **Under no circumstances should a skip event trigger a `song_complete` or `song_pause` event.** The internal state machine must intercept the transition and immediately close the previous song session with a skip event.

---

## 6. Deduplication Rules

Due to the complex lifecycle of Android components, state flow emissions can happen repeatedly. The specification defines the following guard rules to ensure **exactly-once** event emission per real action.

### Safeguards Against Duplicate Triggers
1. **State Transition Caches (Memory)**:
   The tracking component must maintain a memory cache of the last logged state values:
   - `lastLoggedSongId: String?`
   - `lastLoggedState: PlayerState`
   - `lastLoggedIsPlaying: Boolean`
   Events are logged only if the current event is a genuine delta transition. For example, if ExoPlayer sends duplicate `onIsPlayingChanged(true)` callbacks back-to-back, the tracker checks if `lastLoggedIsPlaying == true` and discards the duplicate.
2. **Recomposition & Configuration Change Immunity**:
   Since the events are owned by the `PlayerManager` (a Hilt `@Singleton` application-scoped service), they are completely independent of UI activity. Screen rotations, Compose recompositions, Activity recreations, and fragment transitions do not re-instantiate the playback manager or duplicate listeners, guaranteeing no duplicate events are sent during configuration changes.
3. **App Start & State Restoration Filter**:
   When Musyfy starts, `PlayerManagerImpl` reads the last active track and position from `SharedPreferences` to restore the UI state and prepares ExoPlayer at that position.
   - *Rule*: During state restoration (when `PlayerManagerImpl` is running its restoration coroutine), the tracker must set a flag `isRestoringState = true`. All analytics emissions must be ignored while this flag is true. Once restoration completes and the user initiates a new play action, the flag is set to `false`.
4. **Active Listened Duration Tracking**:
   To prevent double-counting of `listened_duration_ms` when seeking or pausing, the system must compute durations using real-time deltas:
   - Record `systemTime` when playback begins.
   - When a pause, skip, seek, or complete event happens, add the delta `System.currentTimeMillis() - lastActiveTimestamp` to the accumulated `listened_duration_ms`.
   - Clear the accumulated duration whenever a new track loads.

---

## 7. Analytics Naming Standards

To prevent naming drift as new features are added, all future events and parameters must conform to the following standards:

### Naming Conventions
- **Snake Case ONLY**: All event names and parameter names must use lowercase letters with underscores (e.g. `song_play`, `listened_duration_ms`).
- **Clear Prefixing**: Events originating from distinct sources should have a prefix if they are ambiguous. Playback outcome events have no prefix as they are core events.
- **Immutable Events**: Once an event name or parameter key is published to production, it must never be renamed. If a change is required, deprecate the old parameter/event and introduce a new one.
- **Reserved Prefixes**: Do not use `firebase_`, `google_`, or `ga_` prefixes, as these are reserved for Firebase's internal tracking library.
- **Registering Constants**: All new events and parameters must be registered as string constants in `AnalyticsConstants.kt` inside their respective object classes (`Events` and `Params`) to prevent typo bugs.

---

## 8. Future Compatibility

The taxonomy is designed to be easily extensible. Future milestones can build on this foundation without breaking backward compatibility or duplicating code.

### Guidelines for Future Features
* **Sleep Timer**:
  Add an optional parameter `pause_trigger` to `song_pause` (e.g., `pause_trigger = "sleep_timer"`, default value is `"user_manual"`).
* **Crossfade**:
  Add optional parameters `transition_type = "crossfade"` and `crossfade_duration_ms` to `song_play` and `song_skip_next`.
* **Equalizer**:
  Log a new event `equalizer_preset_changed` with parameter `preset_name: String`.
* **Lyrics**:
  Log events `lyrics_opened` and `lyrics_scroll_synchronized` with parameter `song_id: String`.
* **Casting & Connected Devices**:
  Introduce a global event parameter or user property `output_target` (e.g., `"speaker"`, `"bluetooth"`, `"chromecast"`, `"android_auto"`).
* **Wear OS & Android Auto**:
  Introduce a global property `device_platform` (e.g., `"phone"`, `"watch"`, `"car"`) to segregate playback metrics by client type.
* **Cloud Sync**:
  Add `sync_session_id` parameter to trace handoff playbacks between devices.

---

## 9. Developer Guidelines

### Best Practices
- **Observe States, Do Not Intercept Operations**: Track playback by observing `Player.Listener` state transitions. Never hook analytics into the business logic functions like `play()`, `pause()`, or `seekTo()`.
- **Unit Test State Transitions**: When implementing, write unit tests for the tracking state machine using mock ExoPlayer states. Verify that sequence flows like `PLAYING -> BUFFERING -> PLAYING` do not emit duplicate `song_play` events.
- **Always Safely Fallback**: Ensure that any error in calculating metrics (such as division-by-zero on `completion_percentage` for zero-duration streams) is safely caught, logging a default value (e.g., `0.0`) instead of crashing the app.

### Common Mistakes to Avoid
- *Mistake*: Logging `song_play` inside the play button tap handler.
  - *Correction*: Wait for ExoPlayer to change to the playing state.
- *Mistake*: Counting seek buffering time as active listened time.
  - *Correction*: Exclude buffering durations by tracking active playing intervals using system clock time changes.
- *Mistake*: Emitting `song_complete` when the user manually clicks "Next".
  - *Correction*: The state machine must recognize manual skips and emit `song_skip_next` instead of `song_complete`.

---

## 10. Versioning & Change Log

### Specification Metadata
- **Specification Version**: `1.0.0`
- **Phase Number**: `Phase 9.4`
- **Last Updated**: `2026-07-20`
- **Author**: `Antigravity Coding Assistant`

### Future Revision Plan
When modifications are required:
1. Increment the minor version (e.g. `1.0.0` -> `1.1.0`) for additions of optional parameters or new compatible events.
2. Increment the major version (e.g. `1.0.0` -> `2.0.0`) if trigger rules are redesigned or existing parameters are altered.
3. Update the change log below outlining the alterations.

### Change Log
- **v1.0.0** (2026-07-20): Initial release of the official Playback Analytics Specification.
