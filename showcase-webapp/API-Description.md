# API Description #


## Recommendation ##


- *GET* [reco/testcall](reco/testcall)
- *GET* [/reco/dockerstatus](reco/dockerstatus)
- *GET* [/reco/piostatus](reco/piostatus)
- *GET* [/reco/pioevents](reco/pioevents)
- *GET* [/reco/piosimplereco?itemId={ItemID}&length={maxitems}](reco/piosimplereco?itemId={ItemID}&length={maxitems})
- *POST* /reco/createentity
- *GET* [/reco/zoo/{subject_id}/discussion/relatedsubjects](reco/zoo/{subject_id}/discussion/relatedsubjects)
- *GET* [/reco/zoo/{subject_item}/is_debated?chatItem={chat_Item}](reco/zoo/http:%2F%2Fdemo1.mico-project.eu:8080%2marmotta%261af22c9-a8e0-44b9-82c0-c3248f1aa046/is_debated?chatItem=http:%2F%2Fdemo1.mico-project.eu:8080%2Fmarmotta%2Fbac38e61-257b-417e-b2aa-3e1835aa59d2)


## Video Analysis ##

- *POST* /videos
- *GET* [/videos/default](videos/default)
- *GET* [/videos/analyzed](videos/analyzed)
- *GET* [/videos/entities/{entity}](videos/entities/{entity})
- *GET* [/videos/related/{filename}](videos/related/{filename})



## WP5-NER ##

- *GET* [/ner/{source}/entities?querytype=id](ner/{source}/entities?querytype=id)
- *GET* [/ner/{source}/entities?querytype=name](ner/{source}entities?querytype=name)
- *GET* [/ner/{source}/transcript](ner/{source}/transcript)


## Text Analysis ##

- *POST* /zooniverse/textanalysis
- *GET* [/zooniverse/textanalysis/{id}](zooniverse/textanalysis/{id})

## Stuff ##

- *GET* [/logs/catalina](logs/catalina)