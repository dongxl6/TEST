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
		System.out.println("���ǳ�ʼ������");
		scanPackage(config.getInitParameter("scanPackage"));

		doInstance();

		doAutoWired();

		doHandlerMapping();

	}

	private void scanPackage(String pkgName) {
		// ��ȡָ���İ���ʵ��·��url����com.tianyalei.mvc���Ŀ¼�ṹcom/tianyalei/mvc
		URL url = getClass().getClassLoader().getResource("/" + pkgName.replaceAll("\\.", "/"));
		// ת����file����
		File dir = new File(url.getFile());
		// �ݹ��ѯ���е�class�ļ�
		for (File file : dir.listFiles()) {
			// �����Ŀ¼���͵ݹ�Ŀ¼����һ�㣬��com.tianyalei.mvc.controller
			if (file.isDirectory()) {
				scanPackage(pkgName + "." + file.getName());
			} else {
				// �����class�ļ�����������Ҫ��spring�йܵ�
				if (!file.getName().endsWith(".class")) {
					continue;
				}
				// ������className = com.tianyalei.mvc.controller.WebController
				String className = pkgName + "." + file.getName().replace(".class", "");
				// �ж��Ƿ�Controller����Serviceע���ˣ����ûע�⣬��ô���ǾͲ�������Ʃ��annotation����DispatcherServlet�����ǾͲ�����
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
		// �������еı��йܵ��࣬����ʵ����
		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className);
				// �����Controller
				if (clazz.isAnnotationPresent(Controller.class)) {
					// ������webController -> new WebController
					instanceMapping.put(lowerFirstChar(clazz.getSimpleName()), clazz.newInstance());
				} else if (clazz.isAnnotationPresent(Service.class)) {
					// ��ȡע���ϵ�ֵ
					Service service = clazz.getAnnotation(Service.class);
					// ������QueryServiceImpl�ϵ�@Service("myQueryService")
					String value = service.value();
					// �����ֵ�����Ը�ֵΪkey
					if (!"".equals(value.trim())) {
						instanceMapping.put(value.trim(), clazz.newInstance());
					} else {// ûֵʱ���ýӿڵ���������ĸСд
						// ��ȡ���Ľӿ�
						Class[] inters = clazz.getInterfaces();
						// �˴��򵥴����ˣ��ٶ�ServiceImplֻʵ����һ���ӿ�
						for (Class c : inters) {
							// ���� modifyService->new ModifyServiceImpl����
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
		// �������б��йܵĶ���
		for (Map.Entry<String, Object> entry : instanceMapping.entrySet()) {
			// �������б�Autowiredע�������
			// getFields()���ĳ��������еĹ�����public�����ֶΣ���������;
			// getDeclaredFields()���ĳ����������������ֶΣ�������public��private��proteced�����ǲ���������������ֶΡ�
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				// û��autowired�Ĳ���Ҫעֵ
				if (!field.isAnnotationPresent(Autowired.class)) {
					continue;
				}
				String beanName;
				// ��ȡAutoWired����д��ֵ��Ʃ��@Autowired("abc")
				Autowired autowired = field.getAnnotation(Autowired.class);
				if ("".equals(autowired.value())) {
					// �� searchService��ע�⣬�˴��ǻ�ȡ���Ե�����������ĸСд�����������޹أ����Զ���@Autowired SearchService
					// abc�����ԡ�
					beanName = lowerFirstChar(field.getType().getSimpleName());
				} else {
					beanName = autowired.value();
				}
				// ��˽�л���������Ϊtrue,��Ȼ���ʲ���
				field.setAccessible(true);
				// ȥӳ�������Ƿ���ڸ�beanName��Ӧ��ʵ������
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
		// �����йܵĶ���Ѱ��Controller
		for (Map.Entry<String, Object> entry : instanceMapping.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			// ֻ����Controller�ģ�ֻ��Controller��RequestMapping
			if (!clazz.isAnnotationPresent(Controller.class)) {
				continue;
			}

			// ����url
			String url = "/";
			// ȡ��Controller�ϵ�RequestMappingֵ
			if (clazz.isAnnotationPresent(RequestMapping.class)) {
				RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
				url += requestMapping.value();
			}

			// ��ȡ�����ϵ�RequestMapping
			Method[] methods = clazz.getMethods();
			// ֻ�����RequestMapping�ķ���
			for (Method method : methods) {
				if (!method.isAnnotationPresent(RequestMapping.class)) {
					continue;
				}

				RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
				// requestMapping.value()������requestMapping��ע��������ַ�������û�д��д"/"�����Ƕ���������
				String realUrl = url + "/" + methodMapping.value();
				// �滻�������"/",��Ϊ�е��û���RequestMapping��д"/xxx/xx",�еĲ�д���������Ǵ���������"/"
				realUrl = realUrl.replaceAll("/+", "/");

				// ��ȡ���еĲ�����ע�⣬�м����������м���annotation[]��Ϊë�������أ���Ϊһ�����������ж��ע�⡭��
				Annotation[][] annotations = method.getParameterAnnotations();
				// ���ں����Method��invokeʱ����Ҫ�������в�����ֵ�����飬������Ҫ�����������λ��
				/*
				 * ��Search�������⼸������Ϊ�� @RequestParam("name") String name, HttpServletRequest
				 * request, HttpServletResponse response δ����invokeʱ����Ҫ��������������һ������["abc",
				 * request, response]��"abc"������Post������ͨ��request.getParameter("name")����ȡ
				 * Request��response����򵥣���post������ֱ�Ӿ��С� ����������Ҫ����@RequestParam�ϵ�valueֵ��������λ�á�Ʃ��
				 * name->0,ֻ���õ���������ֵ�� ���ܽ�post��ͨ��request.getParameter("name")�õ���ֵ���ڲ�������ĵ�0��λ�á�
				 * ͬ��Ҳ��Ҫ����request��λ��1��response��λ��2
				 */
				Map<String, Integer> paramMap = new HashMap<>();

				// ��ȡ����������в����Ĳ�������ע�⣺�˴�ʹ����ASM.jar
				// �汾Ϊasm-3.3.1����Ҫ��web-inf�½�lib�ļ��У�����asm-3.3.1.jar���������أ�
				// ��Controller��add���������õ���������["name", "addr", "request", "response"]
				String[] paramNames = Play.getMethodParameterNamesByAsm4(clazz, method);

				// ��ȡ���в��������ͣ���ȡRequest��Response������
				Class<?>[] paramTypes = method.getParameterTypes();

				for (int i = 0; i < annotations.length; i++) {
					// ��ȡÿ�������ϵ�����ע��
					Annotation[] anns = annotations[i];
					if (anns.length == 0) {
						// ���û��ע�⣬������String abc��Request request���֣�ûдע���
						// ���û��RequestParamע��
						// �����Request����Response����ֱ����������key���������ͨ���ԣ�����������
						Class<?> type = paramTypes[i];
						if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
							paramMap.put(type.getName(), i);
						} else {
							// ����ûд@RequestParamע�⣬ֻд��String name����ôͨ��java���޷���ȡ��name�����������
							// ͨ������asm��ȡ��paramNames��ӳ��
							paramMap.put(paramNames[i], i);
						}
						continue;
					}

					// ��ע�⣬�ͱ���ÿ�������ϵ�����ע��
					for (Annotation ans : anns) {
						// �ҵ���RequestParamע��Ĳ�������ȡvalueֵ
						if (ans.annotationType() == RequestParam.class) {
							// Ҳ����@RequestParam("name")�ϵ�"name"
							String paramName = ((RequestParam) ans).value();
							// ���@RequestParam("name")������
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
		// ���������URLȥ���Ҷ�Ӧ��method
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
		// �û������ַ
		String requestUri = request.getRequestURI();
		String contextPath = request.getContextPath();
		// �û�д�˶��"///"��ֻ����һ��
		requestUri = requestUri.replace(contextPath, "").replaceAll("/+", "/");

		HandlerModel handlerModel = handlerMapping.get(requestUri);
		if (handlerModel != null) {
			Map<String, Integer> paramIndexMap = handlerModel.paramMap;
			// ����һ������������Ӧ�ø�method�����в�����ֵ������
			Object[] paramValues = new Object[paramIndexMap.size()];

			Class<?>[] types = handlerModel.method.getParameterTypes();

			// ����һ�����������в���[name->0,addr->1,HttpServletRequest->2]
			for (Map.Entry<String, Integer> param : paramIndexMap.entrySet()) {
				String key = param.getKey();
				if (key.equals(HttpServletRequest.class.getName())) {
					paramValues[param.getValue()] = request;
				} else if (key.equals(HttpServletResponse.class.getName())) {
					paramValues[param.getValue()] = response;
				} else {
					// ����û����˲�����Ʃ�� name= "wolf"����һ�²�������ת�������û�������ֵתΪ�����в���������
					String parameter = request.getParameter(key);
					if (parameter != null) {
						paramValues[param.getValue()] = convert(parameter.trim(), types[param.getValue()]);
					}
				}
			}
			// ����÷���
			handlerModel.method.invoke(handlerModel.controller, paramValues);
			return true;
		}

		return false;
	}

	/**
	 * ���û������Ĳ���ת��Ϊ������Ҫ�Ĳ�������
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
			throw new RuntimeException("��֧�ֵĲ���");
		} else {
			// TODO ���кܶ����������ͣ�char��double֮����������ƣ�Ҳ������List<>, Array, Map֮���ת��
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
