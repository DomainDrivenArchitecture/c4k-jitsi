# Logo customizing

Should be put like https://github.com/jitsi-contrib/installers/blob/main/templates/jitsi/usr/share/jitsi-meet/static/branding.json

# video performance issues

[jitsi Helm]: (https://github.com/jitsi-contrib/jitsi-helm)
[Howto override settings-config.js]: (https://github.com/jitsi-contrib/jitsi-helm/blob/a1e029371ed519cda7abde0c616fd92e117eddef/values.yaml#L58)
[10_config]: (https://github.com/jitsi/docker-jitsi-meet/blob/9016f15add8836277201d081c2a67a96ff6c9548/web/rootfs/etc/cont-init.d/10-config#L126)  
Helpful basics:
* [jitsi Helm]  
* [Howto override settings-config.js]  
* [10_config]  

```
Issue: 
eventually low performance cpu settings (clientside, VM?) keeps troubling in audio performance (delays, also interruption) //Todo: jitsi in VM?
```

- something read about: put `config.js` in `/usr/share/jitsi-meet` - in pod at `/config/config.js`
  https://jitsi-club.gitlab.io/jitsi-self-hosting/en/01-deployment-howto/03-tuning/ controversial to [10_config]
- [AV1 jitsi]: https://jitsi.org/blog/av1-and-more-how-does-jitsi-meet-pick-video-codecs/
  [AV1 jitsi]
  Blog, 17.12.2024
    
    Should be discussed:
    ```In a Jitsi conference, the client and JVB work in tandem to ensure efficient video streaming. This involves an ongoing exchange of sender and receiver video constraints.

    Receiver Constraints: 
    Each participant sends constraints (e.g., desired resolution and priority) to the JVB for the video streams they want to receive. These constraints are influenced by the participant’s current layout (e.g., larger tiles for active speakers).

    Sender Constraints: 
    JVB aggregates these constraints and communicates the required video resolutions to the senders. This ensures a participant only sends higher-resolution streams (e.g., 720p) if at least one other participant views them in a sufficiently large tile.
    ```
  "evtentually" there are competing requests from different client-server tasks that differ in `resolution` <-> `videoQuality`  
  -> //Todo: Check logs, grafana?

- What about ff. custom settings, done by gec  
  https://repo.prod.meissa.de/meissa/c4k-jitsi/commit/f4fe85907db422206833ee95796f2b4bcec375a9  
  c4k-jitsi/infrastructure/web/image/resources/settings-config.js, lines 567-end:

  ```js
  // CUSTOM ADJUSTMENTS
    config.disableThirdPartyRequests = true;
    config.channelLastN = 8;
    config.enableLayerSuspension = true; // https://jitsi-club.gitlab.io/jitsi-self-hosting/en/01-deployment-howto/03-tuning/#recommended_enable_layer_suspension
  ```

- https://github.com/jitsi/jitsi-videobridge/issues/1396  
  eventually no memory leak, but how is cpu & memory performance of JVB pod, are there any abnormalities if audio issue occurs? 

## Testserver needed - to be discussed
  1. Are there any changes made to config, after adding ff. to `src/main/resources/jitsi/web-config-envs-cm.yaml`  

    ```yml
    VIDEOQUALITY: "480"  
    VIDEOQUALITY_AV1: "480"  ## AV1 is primary default codec, throttling/performance described in [AV1_jitsi]  
    AUDIO_QUALITY_OPUS_BITRATE: "null" ##set default variable bitrate - beware, noice cancelling, auto level noise, etc., would be turned off  
    ```   
          https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-configuration/#videoquality
          https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-configuration/#audioquality  
          //Todo{low prio}: Maybe we should kick out h.264 ??

  2. `config.js` in `/usr/share/jitsi-meet` -> Todo: Result?

  3. Should default setting 720p `resolution` & `videoQuality` do the job ?? described as default value, eventually that's well tested on jitsi development side.  
  https://jitsi-club.gitlab.io/jitsi-self-hosting/en/01-deployment-howto/03-tuning/#recommended_limit_video_resolution  
  controversial to [AV1 jitsi]  
  
  4. Testing Parameters `config.js`  
  https://github.com/jitsi/jitsi-meet/blob/master/config.js#L88  
    Example:  
    // P2P test mode disables automatic switching to P2P when there are 2  
      // participants in the conference.  
      // p2pTestMode: false,  
  
## Test-server circumstances:  

All circumstances measured by 3rd person on "neutral" extern client within grafana  
In general, every circumstance should be done like ff: 
* 2 participants under normal workload {like weekly presentation}
* 2 participants under normal workload {like weekly presentation} using and "Testserver needed"/4. T.Parameters configured true, see above ```p2pTestMode: true,```   
* 3 participants under normal workload {like weekly presentation}
* automated N participants under normal workload {like weekly presentation}

1. Run at default values `720p`, no entries to any video settings in `web-config-envs-cm.yaml`, branch video_performance_issue commit a94563ff96 from zam 28.02.2025

2. RUN with `480p` in&out   
  - branch `test_480p` commit `0ce1f19935` from zam 28.02.2025

3. Run with `VP9`, as first element of preferedOrder
  - branch should be done

4. well running codec but no `h.264`
  - branch should be done
### Grafana CPU&MEM pods JVB, web & general results
1. 720p  
  Results:  
  Logs:

2. 480p
  Results:  
  Logs:
3. VP9  
  Results:  
  Logs:
4. no h.264  
  Results:  
  Logs:
