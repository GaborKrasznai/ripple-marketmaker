package marketmaker.utils;

import java.net.URLEncoder;
import java.util.Map;

public class Utils {
	private static final char PARAMETER_DELIMITER = '&';
	private static final char PARAMETER_EQUALS_CHAR = '=';

	public static String createQueryStringForParameters(Map<String, String> parameters) {
		StringBuilder parametersAsQueryString = new StringBuilder();
		if (parameters != null) {
			boolean firstParameter = true;

			for (String parameterName : parameters.keySet()) {
				if (!firstParameter) {
					parametersAsQueryString.append(PARAMETER_DELIMITER);
				}

				parametersAsQueryString.append(parameterName).append(PARAMETER_EQUALS_CHAR)
						.append(URLEncoder.encode(parameters.get(parameterName)));

				firstParameter = false;
			}
		}
		return parametersAsQueryString.toString();
	}
}
