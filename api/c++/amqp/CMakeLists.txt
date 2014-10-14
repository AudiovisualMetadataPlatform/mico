include_directories(.)


add_library(mico_amqp SHARED basicheaderframe.h exchangedeleteokframe.h basicackframe.h channelflowokframe.h queueframe.h
                             transactioncommitframe.h methodframe.h channelopenframe.h connectioncloseframe.h
                             connectiontuneframe.h messageimpl.h protocolexception.h channelcloseframe.h
                             basicconsumeframe.h watchable.cpp queuebindframe.h includes.h connectionopenframe.h
                             connectionopenokframe.h basicreturnframe.h array.cpp connectionsecureframe.h field.cpp
                             channelimpl.cpp basicrecoverasyncframe.h transactionselectokframe.h exchangebindokframe.h
                             queueunbindokframe.h basiccancelframe.h exception.h transactionframe.h queuedeclareokframe.h
                             basicgetemptyframe.h basicgetokframe.h connectiontuneokframe.h exchangeframe.h deferredconsumer.cpp
                             channelframe.h exchangedeclareframe.h connectionsecureokframe.h exchangeunbindokframe.h
                             basicrecoverframe.h flags.cpp returnedmessage.h queuebindokframe.h bodyframe.h basicframe.h
                             basicpublishframe.h queuepurgeokframe.h frame.h basiccancelokframe.h extframe.h connectionimpl.cpp
                             deferredcancel.cpp basicqosokframe.h channelcloseokframe.h basicnackframe.h queueunbindframe.h
                             exchangebindframe.h channelopenokframe.h basicrejectframe.h transactionrollbackokframe.h
                             basicdeliverframe.h queuedeclareframe.h exchangedeclareokframe.h heartbeatframe.h
                             connectioncloseokframe.h table.cpp connectionstartframe.h connectionstartokframe.h
                             transactionrollbackframe.h queuepurgeframe.h receivedframe.cpp connectionframe.h
                             queuedeleteframe.h basicgetframe.h headerframe.h protocolheaderframe.h basicconsumeokframe.h
                             consumedmessage.h basicqosframe.h transactionselectframe.h exchangeunbindframe.h
                             exchangedeleteframe.h queuedeleteokframe.h transactioncommitokframe.h basicrecoverokframe.h
                             framecheck.h channelflowframe.h amqpcpp.h)


install(TARGETS mico_amqp LIBRARY DESTINATION lib)
install(FILES basicheaderframe.h exchangedeleteokframe.h basicackframe.h channelflowokframe.h queueframe.h
              transactioncommitframe.h methodframe.h channelopenframe.h connectioncloseframe.h connectiontuneframe.h
              messageimpl.h protocolexception.h channelcloseframe.h basicconsumeframe.h
              queuebindframe.h includes.h connectionopenframe.h connectionopenokframe.h basicreturnframe.h
              connectionsecureframe.h basicrecoverasyncframe.h transactionselectokframe.h exchangebindokframe.h
              queueunbindokframe.h basiccancelframe.h exception.h transactionframe.h queuedeclareokframe.h
              basicgetemptyframe.h basicgetokframe.h connectiontuneokframe.h exchangeframe.h
              channelframe.h exchangedeclareframe.h connectionsecureokframe.h exchangeunbindokframe.h
              basicrecoverframe.h returnedmessage.h queuebindokframe.h bodyframe.h basicframe.h
              basicpublishframe.h queuepurgeokframe.h frame.h basiccancelokframe.h extframe.h
              basicqosokframe.h channelcloseokframe.h basicnackframe.h queueunbindframe.h exchangebindframe.h
              channelopenokframe.h basicrejectframe.h transactionrollbackokframe.h basicdeliverframe.h
              queuedeclareframe.h exchangedeclareokframe.h heartbeatframe.h connectioncloseokframe.h
              connectionstartframe.h connectionstartokframe.h transactionrollbackframe.h queuepurgeframe.h
              connectionframe.h queuedeleteframe.h basicgetframe.h headerframe.h protocolheaderframe.h
              basicconsumeokframe.h consumedmessage.h basicqosframe.h transactionselectframe.h exchangeunbindframe.h
              exchangedeleteframe.h queuedeleteokframe.h transactioncommitokframe.h basicrecoverokframe.h framecheck.h
              channelflowframe.h DESTINATION include/amqpcpp)
install(FILES amqpcpp.h DESTINATION include)