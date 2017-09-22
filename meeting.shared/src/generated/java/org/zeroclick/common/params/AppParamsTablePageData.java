package org.zeroclick.common.params;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications
 * recommended.
 */
@Generated(value = "org.zeroclick.common.params.AppParamsTablePage", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class AppParamsTablePageData extends AbstractTablePageData {

	private static final long serialVersionUID = 1L;

	@Override
	public AppParamsTableRowData addRow() {
		return (AppParamsTableRowData) super.addRow();
	}

	@Override
	public AppParamsTableRowData addRow(int rowState) {
		return (AppParamsTableRowData) super.addRow(rowState);
	}

	@Override
	public AppParamsTableRowData createRow() {
		return new AppParamsTableRowData();
	}

	@Override
	public Class<? extends AbstractTableRowData> getRowType() {
		return AppParamsTableRowData.class;
	}

	@Override
	public AppParamsTableRowData[] getRows() {
		return (AppParamsTableRowData[]) super.getRows();
	}

	@Override
	public AppParamsTableRowData rowAt(int index) {
		return (AppParamsTableRowData) super.rowAt(index);
	}

	public void setRows(AppParamsTableRowData[] rows) {
		super.setRows(rows);
	}

	public static class AppParamsTableRowData extends AbstractTableRowData {

		private static final long serialVersionUID = 1L;
		public static final String paramId = "paramId";
		public static final String key = "key";
		public static final String value = "value";
		public static final String category = "category";
		private Long m_paramId;
		private String m_key;
		private String m_value;
		private String m_category;

		public Long getParamId() {
			return m_paramId;
		}

		public void setParamId(Long newParamId) {
			m_paramId = newParamId;
		}

		public String getKey() {
			return m_key;
		}

		public void setKey(String newKey) {
			m_key = newKey;
		}

		public String getValue() {
			return m_value;
		}

		public void setValue(String newValue) {
			m_value = newValue;
		}

		public String getCategory() {
			return m_category;
		}

		public void setCategory(String newCategory) {
			m_category = newCategory;
		}
	}
}
