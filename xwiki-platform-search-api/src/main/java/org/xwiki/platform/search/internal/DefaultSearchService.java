/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.platform.search.internal;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.ApplicationReadyEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.platform.search.Search;
import org.xwiki.platform.search.SearchException;
import org.xwiki.platform.search.SearchService;
import org.xwiki.platform.search.index.SearchIndex;
import org.xwiki.script.service.ScriptService;

/**
 * Implementation of {@link SearchService}.
 * 
 * @version $Id$
 */
@Component
@Named("search")
@Singleton
public class DefaultSearchService implements ScriptService, EventListener, SearchService
{
    /**
     * Search service.
     */
    private static Search search;

    /**
     * Search indexing service.
     */
    private static SearchIndex searchIndex;

    /**
     * Logger.
     */
    @Inject
    private Logger logger;

    /**
     * Component manager.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Properties.
     */
    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configuration;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#getEvents()
     */
    @Override
    public List<Event> getEvents()
    {
        return Collections.singletonList((Event) new ApplicationReadyEvent());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#getName()
     */
    @Override
    public String getName()
    {
        return "XWiki Search service";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchService#getSearch()
     */
    @Override
    public Search getSearch()
    {
        return search;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchService#getSearchIndex()
     */
    @Override
    public SearchIndex getSearchIndex()
    {
        return searchIndex;
    }

    /**
     * Instantiate Search component based on the component hint.
     * 
     * @param componentHint hint to identify the component implementation.
     * @throws SearchException Exception in case of errors in initialization.
     */
    private void instantiateSearchComponent(String componentHint) throws SearchException
    {
        logger.info("Initializing a Search component with hint[" + componentHint + "]");
        try {

            search = this.componentManager.getInstance(Search.class, componentHint);
            searchIndex = this.componentManager.getInstance(SearchIndex.class, componentHint);

        } catch (ComponentLookupException e) {
            logger.error("Error in creating a Search component with hint[" + componentHint + "] :: " + e.getMessage());
            throw new SearchException("Error creating a Search component with hint[" + componentHint + "]", e);
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#onEvent(org.xwiki.observation.event.Event, java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public void onEvent(Event arg0, Object arg1, Object arg2)
    {
        String componentHint = null;
        try {

            // Read properties.
            componentHint = configuration.getProperty("search.backend");
            logger.info("Search backend specified in the configuration file - [" + componentHint + "]");

            // Instantiate search object.
            instantiateSearchComponent(componentHint);

            // Initialize search index.
            search.initialize();

        } catch (IllegalArgumentException e) {
            logger.error("Unable to initialize search.Configured backend is not supported by XWiki.");
        } catch (SearchException e) {
            logger.error("Exception in intializing Search component with hint [" + componentHint + "]", e.getMessage());
        }
    }

}
