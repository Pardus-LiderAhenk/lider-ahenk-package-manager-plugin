package tr.org.liderahenk.packagemanager.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * A utility class which is responsible for parsing specified Linux package
 * repository URL and returning a set of packages.<br/>
 * <br/>
 * 
 * Repository format for a sample sources.list is explained below:<br/>
 * 
 * deb http://ch.archive.ubuntu.com/ubuntu/ saucy main restricted<br/>
 * deb-src http://ch.archive.ubuntu.com/ubuntu/ saucy main restricted<br/>
 * <br/>
 * 
 * <b>deb:</b> These repositories contain binaries or precompiled packages.
 * These repositories are required for most users.<br/>
 * <b>deb-src:</b> These repositories contain the source code of the packages.
 * Useful for developers.<br/>
 * <b>http://ch.archive.ubuntu.com/ubuntu/:</b> The URI (Uniform Resource
 * Identifier), in this case a location on the internet.<br/>
 * <b>saucy:</b> is the release name of your <b>distribution</b>.<br/>
 * <b>main & restricted:</b> are the section names or components. There can be
 * <b>several component names</b>, separated by spaces.<br/>
 * <br/>
 * 
 * Please visit https://help.ubuntu.com/community/Repositories/CommandLine for
 * more information.
 * 
 */
public abstract class RepoSourcesListParser {
	@SuppressWarnings("restriction")
	public static List<PackageInfo> parseURL(String url, String distribution, String[] components, String architecture,
			String deb) {
		List<PackageInfo> packages = new ArrayList<PackageInfo>();
		for (String component : components) {
			try {
				// Find URL pointing to the package file
				String packageURL = findPackageURL(url, distribution, component, architecture);
				
				// GET package file
				HttpClient client = HttpClientBuilder.create().build();
				HttpGet request = new HttpGet(packageURL);
				HttpResponse response = client.execute(request);
				
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					// Extract package file
					BZip2CompressorInputStream inputStream = new BZip2CompressorInputStream(entity.getContent());
					if (inputStream != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
						if (reader != null) {
							String line = "";
							PackageInfo info = null;
							while ((line = reader.readLine()) != null) {
								// Empty line means new package!
								if (info == null || line.trim().isEmpty()) {
									info = new PackageInfo();
									packages.add(info);
								}
								if (line.trim().isEmpty()) {
									continue;
								}
								String[] tokens = line.split(":", 2);
								
								if(tokens.length > 1) {
									String propertyMethodName = getPropertyMethodName(tokens[0].trim());
									String propertyValue = tokens[1].trim();
									setPropertyValue(info, propertyMethodName, propertyValue);
									info.setSource(
											deb + " " + url + " " + distribution + " " + StringUtils.join(components, " "));
								}
							}
						}
						reader.close();
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				Notifier.error("", "Depo ayrıştırılırken hata ile karşılaşıldı.Depo alanının dopruluğundan emin olup tekrar deneyiniz");
			}
		}
		if (packages != null && packages.size() > 0)
			packages.remove(packages.get(packages.size() - 1));
		return packages;
	}

	private static String findPackageURL(String url, String distribution, String component, String architecture) {
		String packageURL = url;
		if (!url.endsWith("/")) {
			packageURL += "/";
		}
		packageURL += "dists/";
		packageURL += distribution + "/";
		packageURL += component + "/";
		packageURL += "binary-" + architecture + "/Packages.bz2";
		return packageURL;
	}

	private static String getPropertyMethodName(String propertyName) {
		// To avoid violation of java variable declaration, use 'packageName'
		// instead of 'package'
		if ("package".equalsIgnoreCase(propertyName)) {
			return "setPackageName";
		}
		final StringBuilder nameBuilder = new StringBuilder("set");
		boolean capitalizeNextChar = true;
		boolean first = true;
		for (int i = 0; i < propertyName.length(); i++) {
			final char c = propertyName.charAt(i);
			if (!Character.isLetterOrDigit(c)) {
				if (!first) {
					capitalizeNextChar = true;
				}
			} else {
				nameBuilder.append(capitalizeNextChar ? Character.toUpperCase(c) : Character.toLowerCase(c));
				capitalizeNextChar = Character.isDigit(c);
				first = false;
			}
		}
		return nameBuilder.toString();
	}

	private static void setPropertyValue(PackageInfo info, String propertyMethodName, String propertyValue) {
		Method method = null;
		try {
			method = info.getClass().getMethod(propertyMethodName, String.class);
			if (method != null) {
				method.invoke(info, propertyValue);
			}
		} catch (Exception e) {
			// Leaving catch block empty is almost always a bad practice but
			// PackageInfo class does not have all the properties so we'll just
			// ignore NoSuchMethodException here
		}
	}
}