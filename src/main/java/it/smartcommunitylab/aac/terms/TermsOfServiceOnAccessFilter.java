/*
 * Copyright 2023 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.smartcommunitylab.aac.terms;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.core.RealmManager;
import it.smartcommunitylab.aac.core.auth.ExtendedAuthenticationToken;
import it.smartcommunitylab.aac.core.auth.RealmAwareAuthenticationEntryPoint;
import it.smartcommunitylab.aac.core.auth.UserAuthentication;
import it.smartcommunitylab.aac.model.Realm;

public class TermsOfServiceOnAccessFilter extends OncePerRequestFilter {
	private final RequestMatcher termsManagedRequestMatcher = new AntPathRequestMatcher("/terms/**");
	static final String[] SKIP_URLS = { "/api/**", "/html/**", "/js/**", "/lib/**", "/fonts/**", "/italia/**",
			"/i18n/**" };
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final RequestCache requestCache;

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	private RequestMatcher requestMatcher;

	private final RealmManager realmManager;

	public TermsOfServiceOnAccessFilter(RealmManager realmManager) {
		// init request cache as store
		HttpSessionRequestCache cache = new HttpSessionRequestCache();
		this.requestCache = cache;
		this.realmManager = realmManager;
		this.requestMatcher = buildRequestMatcher();
	}

	private RequestMatcher buildRequestMatcher() {
		List<RequestMatcher> antMatchers = Arrays.stream(SKIP_URLS).map(u -> new AntPathRequestMatcher(u))
				.collect(Collectors.toList());

		return new NegatedRequestMatcher(new OrRequestMatcher(antMatchers));

	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	public void setRequestMatcher(RequestMatcher requestMatcher) {
		this.requestMatcher = requestMatcher;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (requestMatcher.matches(request) && requiresProcessing(request)
				&& !termsManagedRequestMatcher.matches(request)) {
			logger.trace("process request for {}", request.getRequestURI());
			System.out.println("process request for {" + request.getRequestURI() + " }");
			UserAuthentication userAuth = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
			ExtendedAuthenticationToken token = CollectionUtils.firstElement(userAuth.getAuthentications());

			if (token == null) {
				logger.error("empty token on authentication success");
				return;
			}

			String realm = token.getRealm();

			if (!realm.equalsIgnoreCase(SystemKeys.REALM_GLOBAL) && !realm.equalsIgnoreCase(SystemKeys.REALM_SYSTEM)
					&& request.getSession().getAttribute("termsManaged") == null) {
				Realm realmEntity = realmManager.findRealm(realm);

				if (request.getSession().getAttribute("refusedTerms") != null
						&& request.getSession().getAttribute("refusedTerms").equals("true")) {
					this.logger.debug("logout user after reset");
					SecurityContextHolder.clearContext();
					request.getSession().removeAttribute("termsManaged");
					request.getSession().removeAttribute("refusedTerms");
					request.setAttribute(RealmAwareAuthenticationEntryPoint.REALM_URI_VARIABLE_NAME, realm);
				} else if ((realmEntity.getTosConfiguration().getConfiguration().containsKey("enableTOS"))
						&& (boolean) realmEntity.getTosConfiguration().getConfiguration().get("enableTOS")) {
					String targetUrl = "/terms/"
							+ realmEntity.getTosConfiguration().getConfiguration().get("approveTOS");
					this.requestCache.saveRequest(request, response);
					this.logger.debug("Redirect to {}", targetUrl);
					System.out.println("Redirect to { " + targetUrl + " }");
					this.redirectStrategy.sendRedirect(request, response, targetUrl);
					return;
				} else if (request.getSession().getAttribute("termsManaged") != null
						&& request.getSession().getAttribute("termsManaged").equals("true")) {
					SavedRequest savedRequest = this.requestCache.getRequest(request, response);
					if (savedRequest != null) {
						logger.debug("restore request from cache");
						this.requestCache.removeRequest(request, response);
						this.redirectStrategy.sendRedirect(request, response, savedRequest.getRedirectUrl());
					}
				}
			}
		}

		chain.doFilter(request, response);
		return;
	}

	private boolean requiresProcessing(HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth instanceof UserAuthentication)) {
			return false;
		}
		return true;
	}

}
