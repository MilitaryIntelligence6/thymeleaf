/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2012, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.expression;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.standard.expression.IStandardConversionService;
import org.thymeleaf.standard.expression.StandardConversionService;
import org.thymeleaf.standard.expression.StandardExpressions;

/**
 * <p>
 *   Utility class containing methods for creating utility
 *   objects that will be included into expression evaluation contexts.
 * </p>
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 1.1
 *
 */
public final class ExpressionEvaluatorObjects {

    
    
    public static final String CONTEXT_VARIABLE_NAME = "ctx";
    public static final String ROOT_VARIABLE_NAME = "root";
    public static final String SELECTION_VARIABLE_NAME = "object";
    public static final String LOCALE_EVALUATION_VARIABLE_NAME = "locale";

    /**
     * @since 2.0.14
     */
    public static final String VARIABLES_EVALUATION_VARIABLE_NAME = "vars";
    
    /**
     * @since 1.1.2
     */
    public static final String HTTP_SERVLET_REQUEST_VARIABLE_NAME = "httpServletRequest";
    
    /**
     * @since 1.1.2
     */
    public static final String HTTP_SESSION_VARIABLE_NAME = "httpSession";
    
    public static final String CALENDARS_EVALUATION_VARIABLE_NAME = "calendars";
    public static final String DATES_EVALUATION_VARIABLE_NAME = "dates";
    public static final String BOOLS_EVALUATION_VARIABLE_NAME = "bools";
    public static final String NUMBERS_EVALUATION_VARIABLE_NAME = "numbers";
    public static final String OBJECTS_EVALUATION_VARIABLE_NAME = "objects";
    public static final String STRINGS_EVALUATION_VARIABLE_NAME = "strings";
    public static final String ARRAYS_EVALUATION_VARIABLE_NAME = "arrays";
    public static final String LISTS_EVALUATION_VARIABLE_NAME = "lists";
    public static final String SETS_EVALUATION_VARIABLE_NAME = "sets";
    public static final String MAPS_EVALUATION_VARIABLE_NAME = "maps";
    public static final String AGGREGATES_EVALUATION_VARIABLE_NAME = "aggregates";
    public static final String MESSAGES_EVALUATION_VARIABLE_NAME = "messages";
    public static final String IDS_EVALUATION_VARIABLE_NAME = "ids";



    private static final ConcurrentHashMap<IStandardConversionService,ConcurrentHashMap<Locale,Map<String,Object>>> BASE_OBJECTS_CACHE =
            new ConcurrentHashMap<IStandardConversionService,ConcurrentHashMap<Locale, Map<String,Object>>>(2, 1.0f, 3);
    private static final IStandardConversionService NULL_CONVERSION_SERVICE = new StandardConversionService();

    
    
    
    private ExpressionEvaluatorObjects() {
        super();
    }

    
    
    
    /**
     * 
     * @since 2.0.9
     */
    public static Map<String,Object> computeEvaluationObjects(
            final IProcessingContext processingContext) {


        final IContext context = processingContext.getContext();

        final IStandardConversionService conversionService =
                (processingContext instanceof Arguments?
                        getOptionalConversionService(((Arguments) processingContext).getConfiguration()) :
                        null);

        final Map<String,Object> variables = computeBaseObjects(conversionService, context.getLocale());

        variables.put(CONTEXT_VARIABLE_NAME, context);
        variables.put(LOCALE_EVALUATION_VARIABLE_NAME, context.getLocale());

        if (context instanceof IWebContext) {
            final IWebContext webContext = (IWebContext) context;
            // This gives access to the HttpServletRequest and HttpSession objects, if they exist
            variables.put(
                    HTTP_SERVLET_REQUEST_VARIABLE_NAME, webContext.getHttpServletRequest());
            variables.put(
                    HTTP_SESSION_VARIABLE_NAME, webContext.getHttpSession());
        }

        final Object evaluationRoot = processingContext.getExpressionEvaluationRoot();

        /*
         * #root and #vars are synonyms
         */
        variables.put(ROOT_VARIABLE_NAME, evaluationRoot);
        variables.put(VARIABLES_EVALUATION_VARIABLE_NAME, evaluationRoot);

        if (processingContext.hasSelectionTarget()) {
            variables.put(SELECTION_VARIABLE_NAME, processingContext.getSelectionTarget());
        } else {
            variables.put(SELECTION_VARIABLE_NAME, evaluationRoot);
        }

        if (processingContext instanceof Arguments) {
            
            final Arguments arguments = (Arguments) processingContext; 
                    
            final Messages messages = new Messages(arguments);
            variables.put(MESSAGES_EVALUATION_VARIABLE_NAME, messages);

            final Ids ids = new Ids(arguments);
            variables.put(IDS_EVALUATION_VARIABLE_NAME, ids);
            
        }
        
        return variables;
        
    }
    

    
    


    
    private static Map<String,Object> computeBaseObjects(final IStandardConversionService conversionService, final Locale locale) {

        // ConcurrentHashMaps do not allow null keys!
        final IStandardConversionService defaultedConversionService =
                (conversionService != null? conversionService : NULL_CONVERSION_SERVICE);

        ConcurrentHashMap<Locale,Map<String,Object>> objectsByLocale = BASE_OBJECTS_CACHE.get(defaultedConversionService);

        if (objectsByLocale == null) {
            objectsByLocale = new ConcurrentHashMap<Locale, Map<String, Object>>(5, 1.0f, 3);
            BASE_OBJECTS_CACHE.put(defaultedConversionService, objectsByLocale);
        }

        Map<String,Object> objects = objectsByLocale.get(locale);

        if (objects == null) {

            objects = new HashMap<String, Object>(30);

            if (locale != null) {
                objects.put(CALENDARS_EVALUATION_VARIABLE_NAME, new Calendars(defaultedConversionService, locale));
                objects.put(DATES_EVALUATION_VARIABLE_NAME, new Dates(defaultedConversionService, locale));
                objects.put(NUMBERS_EVALUATION_VARIABLE_NAME, new Numbers(locale));
                objects.put(STRINGS_EVALUATION_VARIABLE_NAME, new Strings(locale));
            }

            objects.put(BOOLS_EVALUATION_VARIABLE_NAME, new Bools(defaultedConversionService));
            objects.put(OBJECTS_EVALUATION_VARIABLE_NAME, new Objects());
            objects.put(ARRAYS_EVALUATION_VARIABLE_NAME, new Arrays());
            objects.put(LISTS_EVALUATION_VARIABLE_NAME, new Lists());
            objects.put(SETS_EVALUATION_VARIABLE_NAME, new Sets());
            objects.put(MAPS_EVALUATION_VARIABLE_NAME, new Maps());
            objects.put(AGGREGATES_EVALUATION_VARIABLE_NAME, new Aggregates());

            objectsByLocale.put(locale, objects);

        }

        return new HashMap<String, Object>(objects);
        
    }




    private static IStandardConversionService getOptionalConversionService(final Configuration configuration) {
        final Object conversionService =
                configuration.getExecutionAttributes().get(StandardExpressions.STANDARD_CONVERSION_SERVICE_ATTRIBUTE_NAME);
        if (conversionService == null || (!(conversionService instanceof IStandardConversionService))) {
            return null;
        }
        return (IStandardConversionService) conversionService;
    }


}
