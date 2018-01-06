package com.dongxl.springmvc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dongxl.springmvc.annotation.Autowired;
import com.dongxl.springmvc.annotation.Controller;
import com.dongxl.springmvc.annotation.RequestMapping;
import com.dongxl.springmvc.annotation.RequestParam;
import com.dongxl.springmvc.annotation.Service;
import com.dongxl.springmvc.util.Play;

@SuppressWarnings("serial")
public class DispatcherServlet extends HttpServlet {
	private List<String> classNames = new ArrayList<>();
	private Map<String, Object> instanceMapping = new HashMap<>();
	private Map<String, HandlerModel> handlerMapping = new HashMap<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("我是初始化方法");
		scanPackage(config.getInitParameter("scanPackage"));

		doInstance();

		doAutoWired();

		doHandlerMapping();

	}

	private void scanPackage(String pkgName) {
		// 获取指定的包的实际路径url，将com.tianyalei.mvc变成目录结构com/tianyalei/mvc
		URL url = getClass().getClassLoader().getResource("/" + pkgName.replaceAll("\\.", "/"));
		// 转化成file对象
		File dir = new File(url.getFile());
		// 递归查询所有的class文件
		for (File file : dir.listFiles()) {
			// 如果是目录，就递归目录的下一层，如com.tianyalei.mvc.controller
			if (file.isDirectory()) {
				scanPackage(pkgName + "." + file.getName());
			} else {
				// 如果是class文件，并且是需要被spring托管的
				if (!file.getName().endsWith(".class")) {
					continue;
				}
				// 举例，className = com.tianyalei.mvc.controller.WebController
				String className = pkgName + "." + file.getName().replace(".class", "");
				// 判断是否被Controller或者Service注解了，如果没注解，那么我们就不管它，譬如annotation包和DispatcherServlet类我们就不处理
				try {
					Class<?> clazz = Class.forName(className);
					if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)) {
						classNames.add(className);
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			}
		}
	}

	private void doInstance() {
		if (classNames.size() == 0) {
			return;
		}
		// 遍历所有的被托管的类，并且实例化
		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className);
				// 如果是Controller
				if (clazz.isAnnotationPresent(Controller.class)) {
					// 举例：webController -> new WebController
					instanceMapping.put(lowerFirstChar(clazz.getSimpleName()), clazz.newInstance());
				} else if (clazz.isAnnotationPresent(Service.class)) {
					// 获取注解上的值
					Service service = clazz.getAnnotation(Service.class);
					// 举例：QueryServiceImpl上的@Service("myQueryService")
					String value = service.value();
					// 如果有值，就以该值为key
					if (!"".equals(value.trim())) {
						instanceMapping.put(value.trim(), clazz.newInstance());
					} else {// 没值时就用接口的名字首字母小写
						// 获取它的接口
						Class[] inters = clazz.getInterfaces();
						// 此处简单处理了，假定ServiceImpl只实现了一个接口
						for (Class c : inters) {
							// 举例 modifyService->new ModifyServiceImpl（）
							instanceMapping.put(lowerFirstChar(c.getSimpleName()), clazz.newInstance());
							break;
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String lowerFirstChar(String className) {
		char[] chars = className.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}

	private void doAutoWired() {
		if (instanceMapping.isEmpty()) {
			return;
		}
		// 遍历所有被托管的对象
		for (Map.Entry<String, Object> entry : instanceMapping.entrySet()) {
			// 查找所有被Autowired注解的属性
			// getFields()获得某个类的所有的公共（public）的字段，包括父类;
			// getDeclaredFields()获得某个类的所有申明的字段，即包括public、private和proteced，但是不包括父类的申明字段。
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				// 没加autowired的不需要注值
				if (!field.isAnnotationPresent(Autowired.class)) {
					continue;
				}
				String beanName;
				// 获取AutoWired上面写的值，譬如@Autowired("abc")
				Autowired autowired = field.getAnnotation(Autowired.class);
				if ("".equals(autowired.value())) {
					// 例 searchService。注意，此处是获取属性的类名的首字母小写，与属性名无关，可以定义@Autowired SearchService
					// abc都可以。
					beanName = lowerFirstChar(field.getType().getSimpleName());
				} else {
					beanName = autowired.value();
				}
				// 将私有化的属性设为true,不然访问不到
				field.setAccessible(true);
				// 去映射中找是否存在该beanName对应的实例对象
				if (instanceMapping.get(beanName) != null) {
					try {
						field.set(entry.getValue(), instanceMapping.get(beanName));
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void doHandlerMapping() {
		if (instanceMapping.isEmpty()) {
			return;
		}
		// 遍历托管的对象，寻找Controller
		for (Map.Entry<String, Object> entry : instanceMapping.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			// 只处理Controller的，只有Controller有RequestMapping
			if (!clazz.isAnnotationPresent(Controller.class)) {
				continue;
			}

			// 定义url
			String url = "/";
			// 取到Controller上的RequestMapping值
			if (clazz.isAnnotationPresent(RequestMapping.class)) {
				RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
				url += requestMapping.value();
			}

			// 获取方法上的RequestMapping
			Method[] methods = clazz.getMethods();
			// 只处理带RequestMapping的方法
			for (Method method : methods) {
				if (!method.isAnnotationPresent(RequestMapping.class)) {
					continue;
				}

				RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
				// requestMapping.value()即是在requestMapping上注解的请求地址，不管用户写不写"/"，我们都给他补上
				String realUrl = url + "/" + methodMapping.value();
				// 替换掉多余的"/",因为有的用户在RequestMapping上写"/xxx/xx",有的不写，所以我们处理掉多余的"/"
				realUrl = realUrl.replaceAll("/+", "/");

				// 获取所有的参数的注解，有几个参数就有几个annotation[]，为毛是数组呢，因为一个参数可以有多个注解……
				Annotation[][] annotations = method.getParameterAnnotations();
				// 由于后面的Method的invoke时，需要传入所有参数的值的数组，所以需要保存各参数的位置
				/*
				 * 以Search方法的这几个参数为例 @RequestParam("name") String name, HttpServletRequest
				 * request, HttpServletResponse response 未来在invoke时，需要传入类似这样的一个数组["abc",
				 * request, response]。"abc"即是在Post方法中通过request.getParameter("name")来获取
				 * Request和response这个简单，在post方法中直接就有。 所以我们需要保存@RequestParam上的value值，和它的位置。譬如
				 * name->0,只有拿到了这两个值， 才能将post中通过request.getParameter("name")得到的值放在参数数组的第0个位置。
				 * 同理，也需要保存request的位置1，response的位置2
				 */
				Map<String, Integer> paramMap = new HashMap<>();

				// 获取方法里的所有参数的参数名（注意：此处使用了ASM.jar
				// 版本为asm-3.3.1，需要在web-inf下建lib文件夹，引入asm-3.3.1.jar，自行下载）
				// 如Controller的add方法，将得到如下数组["name", "addr", "request", "response"]
				String[] paramNames = Play.getMethodParameterNamesByAsm4(clazz, method);

				// 获取所有参数的类型，提取Request和Response的索引
				Class<?>[] paramTypes = method.getParameterTypes();

				for (int i = 0; i < annotations.length; i++) {
					// 获取每个参数上的所有注解
					Annotation[] anns = annotations[i];
					if (anns.length == 0) {
						// 如果没有注解，则是如String abc，Request request这种，没写注解的
						// 如果没被RequestParam注解
						// 如果是Request或者Response，就直接用类名作key；如果是普通属性，就用属性名
						Class<?> type = paramTypes[i];
						if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
							paramMap.put(type.getName(), i);
						} else {
							// 参数没写@RequestParam注解，只写了String name，那么通过java是无法获取到name这个属性名的
							// 通过上面asm获取的paramNames来映射
							paramMap.put(paramNames[i], i);
						}
						continue;
					}

					// 有注解，就遍历每个参数上的所有注解
					for (Annotation ans : anns) {
						// 找到被RequestParam注解的参数，并取value值
						if (ans.annotationType() == RequestParam.class) {
							// 也就是@RequestParam("name")上的"name"
							String paramName = ((RequestParam) ans).value();
							// 如果@RequestParam("name")这里面
							if (!"".equals(paramName.trim())) {
								paramMap.put(paramName, i);
							}
						}
					}

				}
				HandlerModel model = new HandlerModel(method, entry.getValue(), paramMap);

				handlerMapping.put(realUrl, model);

			}
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 根据请求的URL去查找对应的method
		try {
			boolean isMatcher = pattern(req, resp);
			if (!isMatcher) {
				out(resp, "404 not found");
			}
		} catch (Exception ex) {
			ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
			ex.printStackTrace(new java.io.PrintWriter(buf, true));
			String expMessage = buf.toString();
			buf.close();
			out(resp, "500 Exception" + "\n" + expMessage);
		}
	}

	private void out(HttpServletResponse response, String str) {
		try {
			response.setContentType("application/json;charset=utf-8");
			response.getWriter().print(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean pattern(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (handlerMapping.isEmpty()) {
			return false;
		}
		// 用户请求地址
		String requestUri = request.getRequestURI();
		String contextPath = request.getContextPath();
		// 用户写了多个"///"，只保留一个
		requestUri = requestUri.replace(contextPath, "").replaceAll("/+", "/");

		HandlerModel handlerModel = handlerMapping.get(requestUri);
		if (handlerModel != null) {
			Map<String, Integer> paramIndexMap = handlerModel.paramMap;
			// 定义一个数组来保存应该给method的所有参数赋值的数组
			Object[] paramValues = new Object[paramIndexMap.size()];

			Class<?>[] types = handlerModel.method.getParameterTypes();

			// 遍历一个方法的所有参数[name->0,addr->1,HttpServletRequest->2]
			for (Map.Entry<String, Integer> param : paramIndexMap.entrySet()) {
				String key = param.getKey();
				if (key.equals(HttpServletRequest.class.getName())) {
					paramValues[param.getValue()] = request;
				} else if (key.equals(HttpServletResponse.class.getName())) {
					paramValues[param.getValue()] = response;
				} else {
					// 如果用户传了参数，譬如 name= "wolf"，做一下参数类型转换，将用户传来的值转为方法中参数的类型
					String parameter = request.getParameter(key);
					if (parameter != null) {
						paramValues[param.getValue()] = convert(parameter.trim(), types[param.getValue()]);
					}
				}
			}
			// 激活该方法
			handlerModel.method.invoke(handlerModel.controller, paramValues);
			return true;
		}

		return false;
	}

	/**
	 * 将用户传来的参数转换为方法需要的参数类型
	 */
	private Object convert(String parameter, Class<?> targetType) {
		if (targetType == String.class) {
			return parameter;
		} else if (targetType == Integer.class || targetType == int.class) {
			return Integer.valueOf(parameter);
		} else if (targetType == Long.class || targetType == long.class) {
			return Long.valueOf(parameter);
		} else if (targetType == Boolean.class || targetType == boolean.class) {
			if (parameter.toLowerCase().equals("true") || parameter.equals("1")) {
				return true;
			} else if (parameter.toLowerCase().equals("false") || parameter.equals("0")) {
				return false;
			}
			throw new RuntimeException("不支持的参数");
		} else {
			// TODO 还有很多其他的类型，char、double之类的依次类推，也可以做List<>, Array, Map之类的转化
			return null;
		}
	}

	private class HandlerModel {
		Method method;
		Object controller;
		Map<String, Integer> paramMap;

		public HandlerModel(Method method, Object controller, Map<String, Integer> paramMap) {
			this.method = method;
			this.controller = controller;
			this.paramMap = paramMap;
		}
	}
}
