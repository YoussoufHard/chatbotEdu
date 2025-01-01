-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : mer. 01 jan. 2025 à 11:35
-- Version du serveur : 9.1.0
-- Version de PHP : 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `chatbot_edu`
--

-- --------------------------------------------------------

--
-- Structure de la table `chat_history`
--

DROP TABLE IF EXISTS `chat_history`;
CREATE TABLE IF NOT EXISTS `chat_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `message` text NOT NULL,
  `sender` enum('user','chatbot') NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `chat_history`
--

INSERT INTO `chat_history` (`id`, `message`, `sender`, `timestamp`) VALUES
(37, 'c quoi la difference entre un tabou et mabout et explique moi une pete', 'user', '2024-12-29 14:37:29'),
(38, 'Il semble y avoir une erreur de frappe dans votre question.  Je suppose que vous voulez comparer \"tabou\" et \"mabou\".  Cependant, \"mabou\" n\'est pas un terme courant dans le langage franÃ§ais ou dans d\'autres langues que je connais.  Il est possible qu\'il s\'agisse d\'une faute d\'orthographe, d\'un terme dialectal ou d\'un nÃ©ologisme.\n\nEn revanche, \"tabou\" est un mot bien Ã©tabli.  Il dÃ©signe une interdiction sociale, une rÃ¨gle morale ou religieuse stricte qui interdit de parler, de penser ou d\'agir de certaines maniÃ¨res, sous peine de sanctions, souvent de nature surnaturelle ou religieuse.  Le tabou est entourÃ© d\'un sentiment de crainte, de respect ou d\'horreur.\n\n**Exemples de tabous:**\n\n* Dans certaines cultures, parler des morts est tabou.\n* Dans d\'autres, aborder certains sujets sexuels est tabou.\n* La consommation de certains animaux peut Ãªtre taboue dans certaines sociÃ©tÃ©s.\n\n\nSans plus de contexte sur ce que vous entendez par \"mabou\", je ne peux pas faire de comparaison.  Si vous pouvez fournir plus d\'informations ou rectifier l\'orthographe, je serai ravi de vous aider davantage.\n\nConcernant la phrase \"explique moi une pete\", il est difficile de rÃ©pondre sans savoir ce que vous voulez dire. \"Pete\" peut faire rÃ©fÃ©rence Ã  plusieurs choses, et le contexte est crucial.  Pouvez-vous reformuler votre requÃªte ?\n', 'chatbot', '2024-12-29 14:37:32'),
(35, 'comment faire des bonne chose', 'user', '2024-12-29 14:25:45'),
(31, 'Bonjour', 'user', '2024-12-29 14:01:22'),
(32, 'Bonjour !  Comment puis-je vous aider ?\n', 'chatbot', '2024-12-29 14:01:23'),
(33, 'comment etre le meilleur ??', 'user', '2024-12-29 14:01:38'),
(39, 'comment me connecter a api gemini ??', 'user', '2024-12-29 14:41:48'),
(51, 'salut', 'user', '2024-12-31 22:13:16'),
(52, 'quoi ??', 'user', '2024-12-31 22:13:43'),
(53, 'salut', 'user', '2024-12-31 22:16:28'),
(41, 'Résume moi la vie d\'un etudiant', 'user', '2024-12-29 14:45:01'),
(42, 'La vie d\'un étudiant est un mélange de défis et de récompenses, qui varie grandement selon le niveau d\'études, la discipline, la personnalité et les circonstances personnelles.  En général, elle se caractérise par :\n\n* **Études et apprentissage:**  Le cœur de l\'expérience, impliquant des cours, des lectures, des examens, des travaux de recherche, des projets et des présentations.  Le niveau de difficulté et la charge de travail varient considérablement.\n\n* **Gestion du temps et de l\'organisation:**  Un étudiant doit jongler entre les cours, les révisions, les travaux, les activités sociales et potentiellement un travail à temps partiel.  La gestion efficace du temps et de l\'organisation sont cruciales.\n\n* **Vie sociale et relations:**  Rencontres avec des camarades de classe, participation à des activités étudiantes (associations, clubs, sports), développement de nouvelles amitiés et relations.  Ce domaine peut être très enrichissant, mais aussi exigeant en temps et énergie.\n\n* **Aspects financiers:**  Gestion d\'un budget souvent limité, potentiellement avec un travail à temps partiel pour financer les études et la vie quotidienne.  Les coûts des études, du logement, de la nourriture et des loisirs peuvent être importants.\n\n* **Développement personnel:**  L\'université est une période de croissance personnelle, d\'exploration de ses intérêts, de découverte de soi et du développement de compétences cruciales comme l\'autonomie, la gestion du stress et la résolution de problèmes.\n\n* **Stress et pression:**  La charge de travail, les examens, les échéances et la pression sociale peuvent entraîner du stress et de l\'anxiété.  La gestion du stress est un aspect important de la vie étudiante.\n\nEn résumé, la vie d\'un étudiant est un marathon, non un sprint, exigeant de la discipline, de l\'organisation, de la résilience et de la capacité d\'adaptation.  Mais c\'est aussi une période unique de croissance personnelle, d\'apprentissage et d\'opportunités.\n', 'chatbot', '2024-12-29 14:45:05'),
(43, 'pourquoi sur les reponse de gemi api , 1.5 il y\'a des *** certaine ecriture c a d quoi aussi', 'user', '2024-12-29 14:46:49'),
(44, 'Les caractères \"***\" dans les réponses de l\'API Gemini 1.5 indiquent probablement que certaines parties du texte ont été censurées ou masquées.  Google, l\'entreprise derrière Gemini, utilise cette technique pour plusieurs raisons :\n\n* **Protection de la vie privée:**  L\'API pourrait avoir détecté des informations personnelles identifiables (PII) comme des noms, adresses, numéros de téléphone, etc.  Masquer ces informations est crucial pour respecter la vie privée des individus et se conformer aux réglementations sur la protection des données.\n\n* **Sécurité:** Le modèle pourrait avoir généré du contenu inapproprié, offensant, ou potentiellement dangereux.  Les \"***\" servent alors à supprimer ce contenu pour des raisons de sécurité et pour éviter la propagation d\'informations nuisibles.\n\n* **Prévention de la désinformation:** Gemini pourrait avoir identifié des informations fausses ou trompeuses.  La censure permet d\'éviter la diffusion de cette désinformation.\n\n* **Limitations du modèle:**  Dans certains cas, le modèle pourrait avoir du mal à traiter certaines parties du texte et les remplacer par des \"***\" par défaut.  Ceci indique une limitation de l\'algorithme et n\'est pas forcément une censure intentionnelle.\n\n\nEn résumé, la présence de \"***\" dans les réponses de l\'API Gemini 1.5 signifie que des parties du texte ont été omises, probablement pour des raisons éthiques, légales ou techniques.  Sans plus de contexte sur la requête spécifique qui a généré cette réponse, il est impossible de déterminer la raison exacte de la censure.  Si vous voulez comprendre pourquoi une partie spécifique a été censurée, il serait utile d\'examiner attentivement votre requête et son contexte.\n', 'chatbot', '2024-12-29 14:46:52'),
(45, 'non par exemple si je pause des question il met par exemple  ***bonjour*** comme pour apuuyer ou montre impotance , est ce que c\'est pour mettre en gras ??', 'user', '2024-12-29 14:48:14'),
(46, 'Non, `***bonjour***` n\'est pas une syntaxe pour mettre du texte en gras.  C\'est une syntaxe Markdown (un langage de balisage léger) pour mettre du texte en italique.  Trois astérisques de chaque côté rend le texte *très italique* ou *italique souligné* selon les interprètes Markdown.  Pour mettre du texte en gras en Markdown, il faut utiliser deux astérisques de chaque côté : `**bonjour**`  affiche  **bonjour**.\n\nSi un système répond avec `***bonjour***` à vos questions, c\'est probablement un bug ou une mauvaise configuration de l\'outil.  Il essaie peut-être d\'indiquer une certaine importance, mais il utilise la mauvaise syntaxe.\n', 'chatbot', '2024-12-29 14:48:16'),
(47, 'okey donc les appi renvoi des message en markdown ??', 'user', '2024-12-29 14:49:05'),
(48, 'Certaines API renvoient des messages au format Markdown, mais ce n\'est pas universel.  Cela dépend entièrement de l\'API spécifique que vous utilisez.  Pour savoir si une API particulière renvoie du Markdown, vous devez consulter sa documentation.  La documentation devrait spécifier le format des données qu\'elle retourne (JSON, XML, Markdown, texte brut, etc.).\n', 'chatbot', '2024-12-29 14:49:06'),
(49, 'pour gemini 1.5 il utilise laquel ??', 'user', '2024-12-29 14:49:39'),
(50, 'La question est ambiguë.  \"Gemini 1.5\" fait référence à un modèle linguistique large (LLM) de Google.  Il n\'y a pas un seul \"il\" dont on puisse parler.  Pour savoir quel modèle il *utilise*, il faut préciser \"il\" :\n\n* **\"Il\" fait référence à quel système ou application ?**  Un logiciel particulier, une API, un service web ?  Spécifiez le système qui utilise Gemini 1.5.\n\nSans plus de contexte, je ne peux pas répondre à votre question.  Par exemple, si \"il\" fait référence à Bard,  on sait que Bard utilise des modèles de Google, dont il est possible que Gemini 1.5 fasse partie, mais ce n\'est pas confirmé officiellement.\n', 'chatbot', '2024-12-29 14:49:41');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
