# API Description #


## Recommendation ##


- *GET* [reco/testcall](reco/testcall)
- *GET* [/reco/dockerstatus](reco/dockerstatus)
- *GET* [/reco/piostatus](reco/piostatus)
- *GET* [/reco/pioevents](reco/pioevents)
- *GET* [/reco/piosimplereco?itemId={ItemID}&length={maxitems}](reco/piosimplereco?itemId={ItemID}&length={maxitems})
- *POST* /reco/createentity
- *GET* [/reco/zoo/{subject_id}/discussion/relatedsubjects](reco/zoo/{subject_id}/discussion/relatedsubjects)
- *GET* [/reco/zoo/{subject_id}/is_debated](reco/zoo/{subject_id}/is_debated)


## Video Analysis ##

- *POST* /videos
- *GET* [/videos/default](videos/default)
- *GET* [/videos/analyzed](videos/analyzed)



## WP5-NER ##

- *GET* [/ner/{source}/entities?querytype=id](ner/{source}/entities?querytype=id)
- *GET* [/ner/{source}/entities?querytype=name](ner/{source}entities?querytype=name)
- *GET* [/ner/{source}/transcript](ner/{source}/transcript)


## Text Analysis ##

- *POST* /zooniverse/textanalysis
- *GET* [/zooniverse/textanalysis/{id}](zooniverse/textanalysis/{id})

## Stuff ##

- *GET* [/logs/catalina](logs/catalina)