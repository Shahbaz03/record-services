package com.services.manager;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.services.dao.RecordDao;
import com.services.exception.DaoException;
import com.services.exception.RecordManagerException;
import com.services.model.Record;

/**
 * This is a central point for all Record related operations
 * 
 * @author Shahbaz.Alam
 */
@Component
public class RecordManager {
	
	@Autowired
	private RecordDao recordDao;
	
	/**
	 * The method takes the Record object and passes on to the Dao
	 * If the Update is successful, it fetches all the list of records
	 * for that particular date and returns
	 * 
	 * @return List<Record>
	 */
	public List<Record> saveAndReturnRecords(Record record) throws RecordManagerException{
		List<Record> listRecords = null;
		calculateOtherFieldsOfRecord(record);
		try{
			int result = recordDao.saveRecord(record);
			if(result == 1){
				listRecords = recordDao.getAllRecordForCurrentDate(record.getDate());
			}else{
				throw new RecordManagerException("Failed to Save the Record");
			}
		}catch(DaoException ex){
			throw new RecordManagerException("DaoException Caught in saveAndReturnRecords", ex);
		}		
		return listRecords;
	}
	
	/**
	 * This method will delete the Record from the DB. It takes in the 
	 * Record ID and passes on to the Dao Layer.
	 * 
	 * @param recordId
	 */
	public void deleteRecord(BigDecimal recordId) throws RecordManagerException{
		try{
			recordDao.deleteRecord(recordId);
		}catch(DaoException ex){
			throw new RecordManagerException("DaoException Caught in deleteRecord with Record Id:"+recordId, ex);
		}
	}
	
	/**
	 * This method will update the Record in the DB
	 * It takes the Record object, primarily containing Record Id,
	 * Payment Date, Payment Status and Payment Mode
	 * 
	 * @param record
	 */
	public int updateRecord(Record record) throws RecordManagerException{
		int result = 0;
		try{
			result = recordDao.updateRecord(record);
		}catch(DaoException ex){
			throw new RecordManagerException("DaoException Caught in Updating Record", ex);
		}
		return result;
	}
	
	
	

	private void calculateOtherFieldsOfRecord(Record record) {
		if(record.getNetWeight() != null 
				&& record.getBagQty() != null){
			record.setActualWeight(record.getNetWeight().subtract(record.getBagQty()));
		}
		if(record.getActualWeight() != null 
				&& record.getCropRate() != null){
			record.setTotalCost(record.getActualWeight().multiply(record.getCropRate()));
		}
		if(record.getTotalCost() != null){
			record.setMarketFee(record.getTotalCost().divide(new BigDecimal(100)));
		}
		if(record.getMarketFee() != null){
			BigDecimal val = record.getMarketFee().multiply(new BigDecimal(0.05));
			record.setSupervisionFee(val.divide(new BigDecimal(100)));
		}
		if(record.getMarketFee() != null 
				&& record.getSupervisionFee() != null){
			record.setTotalTax(record.getMarketFee().add(record.getSupervisionFee()));
		}
		
	}

}
