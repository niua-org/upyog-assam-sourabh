package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.FloorUnit;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.Toilet;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ToiletDetailsExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(ToiletDetailsExtract.class);
    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    @Override
    public PlanDetail extract(PlanDetail planDetail) {
        for (Block block : planDetail.getBlocks()) {
            if (block.getBuilding() != null && block.getBuilding().getFloors() != null) {
                for (Floor floor : block.getBuilding().getFloors()) {
                	for (FloorUnit unit : floor.getUnits()) {
                    List<Toilet> toilets = extractToilets(planDetail, block, floor, unit);
                    extractVentilation(planDetail, block, floor, unit, toilets);
                    unit.setToilet(toilets);
                }
            }
        }
       }
        return planDetail;
    }
    
	/*
	 * public PlanDetail extract(PlanDetail planDetail) { for (Block block :
	 * planDetail.getBlocks()) { if (block.getBuilding() != null &&
	 * block.getBuilding().getFloors() != null) { for (Floor f :
	 * block.getBuilding().getFloors()) { List<Toilet> toilets = new ArrayList<>();
	 * String layerName =
	 * String.format(layerNames.getLayerName("LAYER_NAME_BLK_FLR_TOILET"),
	 * block.getNumber(), f.getNumber(), "+\\d");
	 * 
	 * List<String> names = Util.getLayerNamesLike(planDetail.getDoc(), layerName);
	 * 
	 * for (String toiletLayer : names) { List<DXFLWPolyline> toiletMeasurements =
	 * Util.getPolyLinesByLayer(planDetail.getDoc(), toiletLayer);
	 * 
	 * if (!toiletMeasurements.isEmpty()) { Toilet toiletObj = new Toilet();
	 * List<Measurement> toiletMeasurementList = new ArrayList<>();
	 * toiletMeasurements.forEach(toilet -> { Measurement measurementToilet = new
	 * MeasurementDetail(toilet, true);
	 * toiletMeasurementList.add(measurementToilet); });
	 * 
	 * toiletObj.setToilets(toiletMeasurementList); toilets.add(toiletObj); }
	 * 
	 * 
	 * }
	 * 
	 * String toiletVentilationLayer = String.format(
	 * layerNames.getLayerName("LAYER_NAME_BLK_FLR_TOILET_VENTILATION"),
	 * block.getNumber(), f.getNumber(), "+\\d");
	 * 
	 * List<String> ventilationList = Util.getLayerNamesLike(planDetail.getDoc(),
	 * toiletVentilationLayer);
	 * 
	 * int index = 0; for (String ventilationHeightLayer : ventilationList) { // get
	 * height from layer String windowHeightStr =
	 * Util.getMtextByLayerName(planDetail.getDoc(), ventilationHeightLayer);
	 * BigDecimal windowHeight = windowHeightStr != null ? new
	 * BigDecimal(windowHeightStr.replaceAll("WINDOW_HT_M=", "")) : BigDecimal.ZERO;
	 * 
	 * // get widths from that same layer List<BigDecimal> windowWidths =
	 * Util.getListOfDimensionByColourCode(planDetail, ventilationHeightLayer,
	 * DxfFileConstants.INDEX_COLOR_TWO);
	 * 
	 * if (index < toilets.size()) { Toilet toiletObj = toilets.get(index);
	 * toiletObj.setToiletVentilation(windowHeight); if (!windowWidths.isEmpty()) {
	 * toiletObj.setToiletWindowWidth(windowWidths); } }
	 * 
	 * index++; }
	 * 
	 * f.setToilet(toilets); } } }
	 * 
	 * return planDetail; }
	 */
    /**
     * Extracts toilet measurement details from CAD layers.
     *
     * @param planDetail the plan detail object
     * @param block      the block being processed
     * @param floor      the floor being processed
     * @return list of toilets found on this floor
     */
    private List<Toilet> extractToilets(PlanDetail planDetail, Block block, Floor floor, FloorUnit unit) {
        List<Toilet> toilets = new ArrayList<>();
        String layerName = String.format(layerNames.getLayerName("LAYER_NAME_BLK_FLR_UNIT_TOILET"),
                block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d");

        List<String> names = Util.getLayerNamesLike(planDetail.getDoc(), layerName);

        for (String toiletLayer : names) {
            List<DXFLWPolyline> toiletMeasurements = Util.getPolyLinesByLayer(planDetail.getDoc(), toiletLayer);

            if (!toiletMeasurements.isEmpty()) {
                Toilet toiletObj = new Toilet();
                List<Measurement> toiletMeasurementList = new ArrayList<>();
                toiletMeasurements.forEach(toilet -> {
                    Measurement measurementToilet = new MeasurementDetail(toilet, true);
                    toiletMeasurementList.add(measurementToilet);
                });

                toiletObj.setToilets(toiletMeasurementList);
                toilets.add(toiletObj);
            }
        }
        return toilets;
    }
    
    /**
     * Extracts ventilation details (window height and width) and maps them to toilets.
     *
     * @param planDetail the plan detail object
     * @param block      the block being processed
     * @param floor      the floor being processed
     * @param toilets    the list of toilets on this floor
     */
    private void extractVentilation(PlanDetail planDetail, Block block, Floor floor, FloorUnit unit,List<Toilet> toilets) {
        String toiletVentilationLayer = String.format(
                layerNames.getLayerName("LAYER_NAME_BLK_FLR_UNIT_TOILET_VENTILATION"),
                block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d");

        List<String> ventilationList = Util.getLayerNamesLike(planDetail.getDoc(), toiletVentilationLayer);

        int index = 0;
        for (String ventilationHeightLayer : ventilationList) {
            // get height from layer
            String windowHeightStr = Util.getMtextByLayerName(planDetail.getDoc(), ventilationHeightLayer);
            BigDecimal windowHeight = windowHeightStr != null
                    ? new BigDecimal(windowHeightStr.replaceAll("WINDOW_HT_M=", ""))
                    : BigDecimal.ZERO;

            // get widths from that same layer
            List<BigDecimal> windowWidths =
                    Util.getListOfDimensionByColourCode(planDetail, ventilationHeightLayer, DxfFileConstants.INDEX_COLOR_TWO);

            if (index < toilets.size()) {
                Toilet toiletObj = toilets.get(index);
                toiletObj.setToiletVentilation(windowHeight);
                if (!windowWidths.isEmpty()) {
                    toiletObj.setToiletWindowWidth(windowWidths);
                }
            }

            index++;
        }
    }
}