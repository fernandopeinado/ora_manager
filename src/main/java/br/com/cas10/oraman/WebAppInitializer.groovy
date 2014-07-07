package br.com.cas10.oraman

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer

import br.com.cas10.oraman.service.ServiceConfig
import br.com.cas10.oraman.web.WebConfig

class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	@SuppressWarnings(['rawtypes', 'unchecked'])
	protected Class<?>[] getRootConfigClasses() {
		return [ServiceConfig.class]
	}

	@Override
	@SuppressWarnings(['rawtypes', 'unchecked'])
	protected Class<?>[] getServletConfigClasses() {
		return [WebConfig.class]
	}

	@Override
	protected String[] getServletMappings() {
		return ['/ws/*']
	}
}