package eu.mico.platform.testutils;

import com.github.anno4j.model.Target;
import com.github.anno4j.model.impl.selector.FragmentSelector;
import com.github.anno4j.model.impl.targets.SpecificResource;
import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.impl.bodymmm.SpeechToTextBodyMMM;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.mockito.Mockito;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.LangString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MqhMocks {


    public static MICOQueryHelperMMM mockMicoQueryHelper() throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException, RepositoryConfigException {


        List<PartMMM> partMMMList = new ArrayList<>();
        partMMMList.add(prepareSttPart());
        partMMMList.add(prepareSttPart());

        List<ItemMMM> itemList = new ArrayList<>();
        itemList.add(prepareItemMock());


        MICOQueryHelperMMM mockMqh = Mockito.mock(MICOQueryHelperMMM.class);

        when(mockMqh.filterBodyType(anyString())).thenReturn(mockMqh);
        when(mockMqh.filterSelectorType(anyString())).thenReturn(mockMqh);
        when(mockMqh.filterTargetType(anyString())).thenReturn(mockMqh);

        when(mockMqh.getPartsBySourceNameOfAsset(anyString())).thenReturn(partMMMList);
        when(mockMqh.getPartsByAssetName(anyString())).thenReturn(partMMMList);
        when(mockMqh.getPartsBySourceLocationOfAsset(anyString())).thenReturn(partMMMList);
        when(mockMqh.getPartsOfItem(anyString())).thenReturn(partMMMList);

        when(mockMqh.getItemsByFormat(anyString())).thenReturn(itemList);


        return mockMqh;
    }


    private static ItemMMM prepareItemMock() {

        AssetMMM assetMock = prepareAssetMMM();

        ItemMMM itemMock = Mockito.mock(ItemMMM.class);
        when(itemMock.getAsset()).thenReturn(assetMock);

        return itemMock;
    }

    private static AssetMMM prepareAssetMMM() {

        AssetMMM assetMock = Mockito.mock(AssetMMM.class);
        when(assetMock.getName()).thenReturn("filename");

        return assetMock;
    }


    private static PartMMM prepareSttPart() {

        PartMMM retPart = Mockito.mock(PartMMM.class);

        SpeechToTextBodyMMM speechToTextBodyMMMMock = prepareSttBody();
        SpecificResource mockSr = prepareSpecificResource();

        Set<Target> targetSet = new HashSet<>();
        targetSet.add(mockSr);

        when(retPart.getBody()).thenReturn(speechToTextBodyMMMMock);
        when(retPart.getTarget()).thenReturn(targetSet);

        return retPart;
    }

    private static SpecificResource prepareSpecificResource() {

        FragmentSelector mockFr = Mockito.mock(FragmentSelector.class);
        when(mockFr.getValue()).thenReturn("<timecode>");
        SpecificResource mockSr = Mockito.mock(SpecificResource.class);
        when(mockSr.getSelector()).thenReturn(mockFr);

        return mockSr;

    }

    private static SpeechToTextBodyMMM prepareSttBody() {
        SpeechToTextBodyMMM speechToTextBodyMMMMock = mock(SpeechToTextBodyMMM.class);
        when(speechToTextBodyMMMMock.getValue()).thenReturn(new LangString("label", "locale"));

        return speechToTextBodyMMMMock;
    }

}
