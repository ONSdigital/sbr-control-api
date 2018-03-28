package filters

import javax.inject.Inject
import play.api.http.DefaultHttpFilters
import play.filters.gzip.GzipFilter

class Filters @Inject() (
  gzipFilter: GzipFilter,
  responseTimeHeader: XResponseTimeHeaderFilter,
  accessLoggingFilter: AccessLoggingFilter
) extends DefaultHttpFilters(gzipFilter, responseTimeHeader, accessLoggingFilter)
