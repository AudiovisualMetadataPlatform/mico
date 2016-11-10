/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.mico.platform.camel.aggretation;


import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ProtocolStringList;

import eu.mico.platform.event.model.Event.AnalysisEvent;
import eu.mico.platform.event.model.Event.AnalysisRequest;

/**
 * Item Aggregator: aggregates together parts to be analyzed, as a single request
 * 
 * NOTE: the output exchange *is* the exchange received by the following consumers, 
 *       and hence we need to modify the *input* 
 *
 * @version 
 */
public class ItemAggregationStrategy implements AggregationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(ItemAggregationStrategy.class);
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            return newExchange;
        }
        try {
	            AnalysisRequest newEvent = AnalysisRequest.parseFrom(newExchange.getIn().getBody(byte[].class));
	            AnalysisRequest oldEvent = AnalysisRequest.parseFrom(oldExchange.getIn().getBody(byte[].class));
	            ProtocolStringList partUriListOld = oldEvent.getPartUriList();
	            if(partUriListOld != null){
	                    newEvent = AnalysisRequest.newBuilder(newEvent).addAllPartUri(partUriListOld).build();
	                    LOG.debug("extended partList: {}", newEvent.getPartUriList());
	            }
	            newExchange.getIn().setBody(newEvent.toByteArray());

        } catch (InvalidProtocolBufferException | NullPointerException e) {
            // TODO Auto-generated catch block
            LOG.error("Unexpected error: ",e);
        }
        return newExchange;
    }

}
