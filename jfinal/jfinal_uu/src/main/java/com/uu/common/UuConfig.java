package com.uu.common;


import com.uu.common.model._MappingKit;
import com.uu.controller.IndexController;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.server.undertow.UndertowServer;
import com.jfinal.template.Engine;

public class UuConfig extends JFinalConfig {

	static Prop p;
	public static void main(String[] args) {
		UndertowServer.start(UuConfig.class);
	}

	/**从左到右依次去找配置，找到则立即加载并立即返回，后续配置将被忽略	 */
	static void loadConfig() {
		if (p == null) {
//		这是PropKit工具类读取外部键值对配置文件.比如PropKit.use("config.txt");
//		String userName = PropKit.get("userName");第一次用use加载的配置将成为主配置，可以通过get()直接取值
//		第二次： Prop p =PropKit.use("db_config.txt");p.get()
			p = PropKit.useFirstFound("demo-config-pro.txt", "demo-config-dev.txt");
		}
	}
	/**配置常量*/
	public void configConstant(Constants me) {
		loadConfig();
		me.setDevMode(p.getBoolean("devMode", false));
		me.setInjectDependency(true);
		me.setInjectSuperClass(true);
	}

	@Override
	public void configRoute(Routes routes) {
		routes.add(new FrontRoutes());  // 前台路由
		routes.add(new AdminRoutes());  // 后台路由
	}
	public class FrontRoutes extends Routes{
		@Override
		public void config() {
		}
	}
	public class AdminRoutes extends Routes {
		@Override
		public void config() {
//		让render(...)参数省去baseViewPath这部分前缀
//			setBaseViewPath("/view/front");
			add("/", IndexController.class);
			//add("/blog", BlogController.class);
		}
	}
	public void configEngine(Engine me) {
		me.addSharedFunction("/common/_layout.html");
		me.addSharedFunction("/common/_paginate.html");
	}

	/**配置插件*/
	public void configPlugin(Plugins me) {
		// 配置 druid 数据库连接池插件P.get()方法是从静态资源文件读取配置的数据库连接参数
		DruidPlugin druidPlugin = new DruidPlugin(p.get("jdbcUrl"), p.get("user"), p.get("password").trim());
		me.add(druidPlugin);

		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		//arp.addMapping("user",User.class);//将user（数据库表名）与User（模型类）映射关系保存到arp中
		// 所有映射在 MappingKit 中自动化搞定
		_MappingKit.mapping(arp);
		me.add(arp);
	}
	public static DruidPlugin createDruidPlugin() {
		loadConfig();

		return new DruidPlugin(p.get("jdbcUrl"), p.get("user"), p.get("password").trim());
	}

	/** 配置全局拦截器*/
	public void configInterceptor(Interceptors me) {

	}

	/**配置处理器:思路就是Handler中改变第一个参数String target的值。*/
	public void configHandler(Handlers me) {

	}
}