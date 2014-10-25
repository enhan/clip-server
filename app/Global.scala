import play.api.mvc.WithFilters
import play.api.{Application, GlobalSettings}
import utils.CORSFilter

/**
 * @author Emmanuel Nhan
 */
object Global extends WithFilters(CORSFilter) with GlobalSettings {

}


