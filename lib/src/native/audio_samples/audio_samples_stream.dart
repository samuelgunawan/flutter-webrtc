import 'dart:async';

import 'package:flutter/services.dart';

import '../utils.dart';
import 'audio_samples.dart';

class AudioSamplesStream {

  MethodChannel get _channel => WebRTC.methodChannel();
  static const EventChannel _eventChannel = EventChannel(
      'FlutterWebRTC/InputAudioSamples');

  Future<Stream<AudioSamples>> get audioStream async {
    await _channel.invokeMethod('attachAudioInputListener');
    return _eventChannel
        .receiveBroadcastStream()
        .map((event) =>
        AudioSamples.fromJson(Map<String, dynamic>.from(event)));
  }

  Future<void> start() async {
    await _channel.invokeMethod('startAudioInputListener');
  }

  Future<void> pause() async {
    await _channel.invokeMethod('pauseAudioInputListener');
  }

  Future<void> dispose() async {
    await _channel.invokeMethod('disposeAudioInputListener');
  }

}
