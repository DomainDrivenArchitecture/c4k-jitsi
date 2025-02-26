# convention 4 kubernetes: c4k-jitsi
[![Clojars Project](https://img.shields.io/clojars/v/org.domaindrivenarchitecture/c4k-jitsi.svg)](https://clojars.org/org.domaindrivenarchitecture/c4k-jitsi) [![pipeline status](https://gitlab.com/domaindrivenarchitecture/c4k-jitsi/badges/master/pipeline.svg)](https://gitlab.com/domaindrivenarchitecture/c4k-jitsi/-/commits/main) 

[<img src="https://domaindrivenarchitecture.org/img/delta-chat.svg" width=20 alt="DeltaChat"> chat over e-mail](mailto:buero@meissa-gmbh.de?subject=community-chat) | [<img src="https://meissa.de/images/parts/contact/mastodon36_hue9b2464f10b18e134322af482b9c915e_5501_filter_14705073121015236177.png" width=20 alt="M"> meissa@social.meissa-gmbh.de](https://social.meissa-gmbh.de/@meissa) | [Blog](https://domaindrivenarchitecture.org) | [Website](https://meissa.de)

## Purpose

c4k-jitsi provides a k8s deployment for jitsi containing [see also Jitsi Architecture](https://jitsi.github.io/handbook/docs/architecture/)
* jitsi-web
* jvb
* jicofo
* prosody
* etherpad for shared documents
* excalidraw for shared whiteboards
* coturn as stun server
* moderator-election as tool for electing the next moderator
* ingress having a letsencrypt managed certificate
* monitoring connected to grafana cloud

The package is intended for a low-load scenario.

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

* https://codeberg.org/meissa/c4k-jitsi (Issues and PR)
* https://gitlab.com/domaindrivenarchitecture/c4k-jitsi (CI)
* https://github.com/DomainDrivenArchitecture/c4k-jitsi

For more details about our repository model see: https://repo.prod.meissa.de/meissa/federate-your-repos

## License

Copyright © 2021, 2022, 2023, 2024, 2025 meissa GmbH
Licensed under the [Apache License, Version 2.0](LICENSE) (the "License")
Pls. find licenses of our subcomponents [here](doc/SUBCOMPONENT_LICENSE)
