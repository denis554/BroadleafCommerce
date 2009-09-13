/*
 * Created on 13-Feb-2005
 *
 */
package org.broadleafcommerce.util.sql;

import org.hibernate.tool.hbm2x.Exporter;
import org.hibernate.tool.hbm2x.Hbm2DDLExporter;

/**
 * This is a re-worked version from Hibernate tools
 * 
 * @author jfischer
 *
 */
public class Hbm2DDLExporterTask extends ExporterTask {

	boolean exportToDatabase = true; 
	boolean scriptToConsole = true;
	boolean schemaUpdate = false;
	String delimiter = ";"; 
	boolean drop = false;
	boolean create = true;
	boolean format = false;
	
	private boolean haltOnError = false;
	
	public Hbm2DDLExporterTask(HibernateToolTask parent) {
		super(parent);
	}
	
	public String getName() {
		return "hbm2ddl (Generates database schema)";
	}

	protected Exporter configureExporter(Exporter exp) {
		Hbm2DDLExporter exporter = (Hbm2DDLExporter) exp;
		exporter.setExport(exportToDatabase);
		exporter.setConsole(scriptToConsole);
		exporter.setUpdate(schemaUpdate);
		exporter.setDelimiter(delimiter);
		exporter.setDrop(drop);
		exporter.setCreate(create);
		exporter.setFormat(format);
		exporter.setOutputFileName(outputFileName);
		exporter.setHaltonerror(haltOnError);		
		return exporter;
	}

	protected Exporter createExporter() {
		Hbm2DDLExporter exporter = new Hbm2DDLExporter(getConfiguration(), getDestdir());
		return exporter;
	}

	
	public void setExport(boolean export) {
		exportToDatabase = export;
	}
	
	/**
	 * Run SchemaUpdate instead of SchemaExport
	 */
	public void setUpdate(boolean update) {
		this.schemaUpdate = update;
	}
	
	/**
	 * Output sql to console ? (default true)
	 */
	public void setConsole(boolean console) {
		this.scriptToConsole = console;
	}
	
	/**
	 * Format the generated sql
	 */
	public void setFormat(boolean format) {
		this.format = format;
	}

	public void setDrop(boolean drop) {
		this.drop = drop;
	}
	
	public void setCreate(boolean create) {
		this.create = create;
	}
	
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	public String getDelimiter() {
		return delimiter;
	}
	
	public void setHaltonerror(boolean haltOnError) {
		this.haltOnError  = haltOnError;
	}

}
