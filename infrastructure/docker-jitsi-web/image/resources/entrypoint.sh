#!/bin/sh
set -eu

echo "
config.disableThirdPartyRequests = true;
config.defaultLanguage = 'de';
config.resolution = 480;
config.constraints.video = {  
      aspectRatio: 16 / 9,   
      height: {    
         ideal: 480,   
         max: 480,    
         min: 240     
      };
config.channelLastN = 8;
config.enableLayerSuspension = true;
config.disableAudioLevels = true;
" >> /defaults/config.js

/init