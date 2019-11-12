
package n.baldyoung.FileDataOption;
import java.io.*; 
import java.util.*;

/**
 * 文件数据操作模块，
 */
public class FileDataOptionModule implements Runnable{
	/**
	 * 数据写入文件时的方式（{COVER:覆盖原文件, APPEND:追加到原文件的尾部}）
	 */
	public enum ActionType{
		COVER, APPEND
	}
	//
	static private volatile Integer Number = 0;
	private InputStream dataSource;
	private String pathName;
	private ActionType actionType;
	public FileDataOptionModule(InputStream tDataSource, String tPathName, String tFileType, ActionType tActioinType){
		this.dataSource = tDataSource;
		this.pathName = tPathName+tFileType;
		this.actionType = tActioinType;
	}
	public FileDataOptionModule(InputStream tDataSource, String tPathName, String tFileType){
		this(tDataSource, tPathName, tFileType, ActionType.COVER);
	}
	public FileDataOptionModule(InputStream tDataSource, String tFileType){
		this(tDataSource, getRandomUniqueIdentificationWithSync(), tFileType);
	}
	public FileDataOptionModule(InputStream tDataSource){
		this(tDataSource, getRandomUniqueIdentificationWithSync(), "");
	}
	public String getPathName(){
		return this.pathName;
	}
	public InputStream getDataSource(){
		return this.dataSource;
	}
	public ActionType getActionType(){
		return this.actionType;
	}
	public boolean saveData() throws IOException, FileNotFoundException{
		boolean result = false;
		if(null == this.dataSource || null == this.pathName)
			result = false;
		else{
			File file = new File(this.pathName);
			if(!file.exists()){
				file.createNewFile();
			}
			if(file.isDirectory() || !file.canWrite()){
				result = false;
			}else{
				FileOutputStream output = new FileOutputStream(adjustPathNameSeparator(this.pathName), (this.actionType != ActionType.APPEND ? false : true) );
				result = copyData(this.dataSource, output);
			}
		}
		return result;
	}
	public void run(){
		try{
			saveData();
		}catch(Exception e){
		}
	}
		
//static method	
	//**************** 将给定的inputstream集合生成FileDataOption集合
	static public List<FileDataOptionModule> getListByInputStreams(List<InputStream> list, String path, String fileType){
		List<FileDataOptionModule> result = new LinkedList<>();
		if(null != list){
			for(InputStream input : list){
				result.add(new FileDataOptionModule(input, getRandomUniqueIdentificationWithSync(path), fileType));
			}
		}
		return result;
	}
	
	//**************** 获取随机标识
	/**
		static Method -- 获取一个随机的标识字符串（通过返回时间戳的形式）
		非线程安全的，因为在获取时间戳的过程有低概率出现多个线程获取的相同时间戳的情况。
		@param headStr 在返回的标识串前加上指定的字符串。
		@return 返回一个在头部加上了指定字符串的标识串。
	*/
	static public String getRandomUniqueIdentification(String headStr){
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(headStr);
		strBuilder.append(System.currentTimeMillis());
		synchronized(FileDataOptionModule.Number){
			strBuilder.append(FileDataOptionModule.Number);
			if(100000 < FileDataOptionModule.Number)
				FileDataOptionModule.Number = 0;
			else FileDataOptionModule.Number++;
		}
		return strBuilder.toString();
	}
	/**
		static Method -- 获取一个随机的标识字符串（通过返回时间戳的形式）
		非线程安全的，因为在获取时间戳的过程有低概率出现多个线程获取的相同时间戳的情况。
		@return 返回一个随机的标识串。
	*/
	static public String getRandomUniqueIdentification(){
		return getRandomUniqueIdentification("");
	}
	/**
		static Method -- 获取一个随机的标识字符串（通过返回时间戳的形式）
		线程安全的，因为在获取时间戳的过程有低概率出现多个线程获取的相同时间戳的情况。
		@param headStr 在返回的标识串前加上指定的字符串。
		@return 返回一个在头部加上了指定字符串的标识串。
	*/
	static public synchronized String getRandomUniqueIdentificationWithSync(String headStr){
		return getRandomUniqueIdentification(headStr);
	}
	/**
		static Method -- 获取一个随机的标识字符串（通过返回时间戳的形式）
		线程安全的，因为在获取时间戳的过程有低概率出现多个线程获取的相同时间戳的情况。
		@return 返回一个随机的标识串。
	*/
	static public synchronized String getRandomUniqueIdentificationWithSync(){
		return getRandomUniqueIdentification("");
	}
	//**************** 数据拷贝调用
	/** 
		static Method -- 数据拷贝调用，从给定的数据源获取数据，并写入到指定的数据存储源。
		@param input 数据来源
		@param output 数据存储源
		@return 数据拷贝成功：true；拷贝失败：false；
		@exception IOException 数据读取或写入异常
	*/
	static public boolean copyData(InputStream input, OutputStream output) throws IOException{
		boolean result = true;
		if(null == input || null == output)
			result = false;
		else{
			try(InputStream src = input; OutputStream dest = output){
				byte data[] = new byte[2048];
				int length; 
				while((length = src.read(data)) != -1){
					dest.write(data, 0, length);
				}
			}
		}
		return result;
	}
	//**************** 路径分隔符校正调用
	/** 
		static Method -- 因为不同的操作系统上，其所采用的路径分隔符不一样，该调用用于自动校正pathName参数中的路径分隔符。
	*/
	static public String adjustPathNameSeparator(String pathName){
		String separator = File.separator;
		String result = null;
		if("/".equals(separator))
			result = pathName.replace("\\", "/");
		else
			result = pathName.replace("/", "\\");
		return result;
	}
	
}
