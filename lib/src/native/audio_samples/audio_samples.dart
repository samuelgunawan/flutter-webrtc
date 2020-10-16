
class AudioSamples {
  AudioSamples({
    this.audioFormat,
    this.channelCount,
    this.sampleRate,
    this.data,
    this.dB,
    this.diff,
  });

  factory AudioSamples.fromJson(Map<String, dynamic> json) => AudioSamples(
    audioFormat: json['audioFormat'],
    channelCount: json['channelCount'],
    sampleRate: json['sampleRate'],
    data: json['data'] == null ? null : List<int>.from(json['data'].map((x) => x)),
    dB: json['dB']?.toDouble(),
    diff: json['diff']?.toDouble(),
  );

  int audioFormat;
  int channelCount;
  int sampleRate;
  List<int> data;
  double dB;
  double diff;
  
  Map<String, dynamic> toJson() => {
    'audioFormat': audioFormat,
    'channelCount': channelCount,
    'sampleRate': sampleRate,
    'data': data == null ? null : List<dynamic>.from(data.map((x) => x)),
    'dB': dB,
    'diff': diff,
  };
}