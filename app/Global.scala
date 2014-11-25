import models.{DBAssignmentDao, DBSongDao, AssignmentGenerator, Song}
import play.api.Mode
import play.api.mvc.WithFilters
import play.api.{Application, GlobalSettings}
import utils.CORSFilter

/**
 * @author Emmanuel Nhan
 */
object Global extends WithFilters(CORSFilter) with GlobalSettings {


  override def onStart(app: Application): Unit = {
    super.onStart(app)
    if (app.mode == Mode.Dev)
      loadInitialData()

  }

  def loadInitialData(): Unit ={

    val tribueDeDana = """Le vent souffle sur les plaines de la Bretagne armoricaine,
      | je jette un dernier regard sur ma femme, mon fils et mon domaine.
      |Akim, le fils du forgeron est venu me chercher,
      |les druides ont décidé de mener le combat dans la vallée.
      |Là, où tous nos ancêtres, de géants guerriers celtes,
      |après de grandes batailles, se sont imposés en maîtres,
      |c'est l'heure maintenant de défendre notre terre
      |contre une armée de Simeriens prête à croiser le fer.
      |Toute la tribu s'est réunie autour de grands menhirs,
      |pour invoquer les dieux afin qu'ils puissent nous bénir.
      |Après cette prière avec mes frères sans faire état de zèle,
      |les chefs nous ont donné à tous des gorgées d'hydromel,
      |pour le courage, pour pas qu'il y ait de faille,
      |pour rester grands et fiers quand nous serons dans la bataille
      |car c'est la première fois pour moi que je pars au combat
      |et j'espère être digne de la tribu de Dana.
      |
      |Dans la vallée de Dana La Lilala.
      |Dans la vallée j'ai pu entendre des échos.
      |Dans la vallée de Dana La Lilala.
      |Dans la vallée des chants de guerre près des tombeaux.
      |
      |Après quelques incantations de druides et de magie,
      |toute la tribu, le glaive en main courait vers l'ennemi,
      |la lutte était terrible et je ne voyais que les ombres,
      |tranchant l'ennemi qui revenait toujours en surnombre.
      |Mes frères tombaient l'un après l'autre devant mon regard,
      |sous le poids des armes que possédaient tous ces barbares,
      |des lances, des haches et des épées dans le jardin d'Eden
      |qui écoulait du sang sur l'herbe verte de la plaine.
      |Comme ces jours de peine où l'homme se traîne
      |à la limite du règne du mal et de la haine,
      |fallait-il continuer ce combat déjà perdu ?
      |mais telle était la fierté de toute la tribu,
      |la lutte a continué comme ça jusqu'au soleil couchant,
      |de férocité extrême en plus d'acharnement,
      |fallait défendre la terre de nos ancêtres enterrés là
      |et pour toutes les lois de la tribu de Dana.
      |
      |Dans la vallée de Dana La Lilala.
      |Dans la vallée j'ai pu entendre des échos.
      |Dans la vallée de Dana La Lilala.
      |Dans la vallée des chants de guerre près des tombeaux.
      |
      |Au bout de la vallée on entendait le son d'une corne,
      |d'un chef ennemi qui rappelait toute sa horde,
      |avait-il compris qu'on lutterait même en enfer
      |et qu'à la tribu de Dana appartenaient ces terres.
      |Les guerriers repartaient, je ne comprenais pas
      |tout le chemin qu'ils avaient fait pour en arriver là,
      |quand mon regard se posa tout autour de moi,
      |j'étais le seul debout de la tribu voilà pourquoi.
      |Mes doigts se sont écartés tout en lâchant mes armes
      |et le long de mes joues se sont mises à couler des larmes,
      |je n'ai jamais compris pourquoi les dieux m'ont épargné
      |ce jour noir de notre histoire que j'ai contée.
      |Le vent souffle toujours sur la Bretagne armoricaine
      |et j'ai rejoins ma femme, mon fils et mon domaine,
      |j'ai tout reconstruit de mes mains pour en arriver là,
      |je suis devenu roi de la tribu de Dana.
      |
      |Dans la vallée de Dana La Lilala.
      |Dans la vallée j'ai pu entendre des échos.
      |Dans la vallée de Dana La Lilala.
      |Dans la vallée des chants de guerre près des tombeaux.""".stripMargin

    val song = DBSongDao.create(Song(None, "La tribu de Dana", "https://www.youtube.com/watch?v=80_t0ssDHQY"))
    val generator = new AssignmentGenerator(song.id.get, 3)
    generator.parseSong(tribueDeDana).foreach(DBAssignmentDao.create)

  }

}





