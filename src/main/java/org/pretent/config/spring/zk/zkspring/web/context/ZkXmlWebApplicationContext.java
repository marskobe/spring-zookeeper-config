package org.pretent.config.spring.zk.zkspring.web.context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.I0Itec.zkclient.ZkClient;
import org.apache.log4j.Logger;
import org.pretent.config.spring.zk.zkspring.ZkObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 继承XmlWebApplicationContext
 * 
 * 复写Resource[] getResources方法，初始化时从网络zk上读取配置
 * 
 * @author root
 *
 */
public class ZkXmlWebApplicationContext extends XmlWebApplicationContext {

	/**
	 * 需要取得的资源
	 */
	private Resource[] resources = null;

	/**
	 * zk.properties的配置，zk 连接属性等
	 */
	private final static Properties properties = new Properties();

	static {
		ResourceBundle bundle = ResourceBundle.getBundle("zk");
		Set<String> keys = bundle.keySet();
		System.out.println("zk.properties:");
		for (String key : keys) {
			properties.put(key, bundle.getObject(key));
			System.out.println(bundle.getString(key));
		}
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public static String getZkServers() {
		return properties.getProperty("zk.servers");
	}

	// 从zk上读取配置信息数据
	private ZkClient zkClient = new ZkClient(
			properties.getProperty("zk.servers"));

	public Resource[] getResources() {
		return resources;
	}

	public void setResources(Resource[] resources) {
		this.resources = resources;
	}

	public ZkClient getZkClient() {
		return zkClient;
	}

	public void setZkClient(ZkClient zkClient) {
		this.zkClient = zkClient;
	}

	@Override
	public Resource[] getResources(String locationPattern) throws IOException {
		System.out.println("getResources.locationPattern------------->"
				+ locationPattern);
		// 启动时兼容本地配置
		if (!locationPattern.startsWith("zk:/")) {
			return super.getResources(locationPattern);
		}
		// 刷新时直接设置
		if (resources != null) {
			return resources;
		}
		// 第一次从网络zk上读取
		return initResourceFromZk(locationPattern);
	}

	/**
	 * 首次从zk上读取数据
	 * 
	 * @param location
	 * @return
	 */
	private Resource[] initResourceFromZk(String location) {
		Resource[] rses = null;
		try {
			System.out.println(location.substring(3));
			ZkObject obj = zkClient.readData(location.substring(3));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Logger.getLogger(this.getClass()).debug(obj.getData());
			rses = new Resource[] { new ByteArrayResource(obj.getData()
					.getBytes()) };
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rses;
	}
}
