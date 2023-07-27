# convention 4 kubernetes: c4k-jitsi
[![Clojars Project](https://img.shields.io/clojars/v/org.domaindrivenarchitecture/c4k-jitsi.svg)](https://clojars.org/org.domaindrivenarchitecture/c4k-jitsi) [![pipeline status](https://gitlab.com/domaindrivenarchitecture/c4k-jitsi/badges/master/pipeline.svg)](https://gitlab.com/domaindrivenarchitecture/c4k-jitsi/-/commits/main) 

[<img src="https://domaindrivenarchitecture.org/img/delta-chat.svg" width=20 alt="DeltaChat"> chat over e-mail](mailto:buero@meissa-gmbh.de?subject=community-chat) | [<img src="https://meissa-gmbh.de/img/community/Mastodon_Logotype.svg" width=20 alt="team@social.meissa-gmbh.de"> team@social.meissa-gmbh.de](https://social.meissa-gmbh.de/@team) | [Website & Blog](https://domaindrivenarchitecture.org)

## Purpose

c4k-jitsi provides a k8s deployment for jitsi containing:
* jitsi
* ingress having a letsencrypt managed certificate

The package aims to a low load sceanrio.

## Status

Stable - we use this setup on production.

## Try out

Click on the image to try out live in your browser:

[![Try it out](doc/tryItOut.png "Try out yourself")](https://domaindrivenarchitecture.org/pages/dda-provision/c4k-jitsi/)

Your input will stay in your browser. No server interaction is required.

You will also be able to try out on cli:
```
target/graalvm/c4k-jitsi src/test/resources/jitsi-test/valid-config.yaml src/test/resources/jitsi-test/valid-auth.yaml | kubeval -
target/graalvm/c4k-jitsi src/test/resources/jitsi-test/valid-config.yaml src/test/resources/jitsi-test/valid-auth.yaml | kubectl apply -f -
```

## Documentation
* [Example Setup on Hetzner](doc/SetupOnHetzner.md)
* [Development](doc/Development.md)

## Development & mirrors

Development happens at: https://repo.prod.meissa.de/meissa/c4k-jitsi

Mirrors are:

* https://gitlab.com/domaindrivenarchitecture/c4k-jitsi (issues and PR, CI)

For more details about our repository model see: https://repo.prod.meissa.de/meissa/federate-your-repos

## License

Copyright © 2022 meissa GmbH
Licensed under the [Apache License, Version 2.0](LICENSE) (the "License")
Pls. find licenses of our subcomponents [here](doc/SUBCOMPONENT_LICENSE)