-- phpMyAdmin SQL Dump
-- version 4.2.12deb2+deb8u3
-- http://www.phpmyadmin.net
--
-- Počítač: localhost
-- Vytvořeno: Sob 18. kvě 2019, 20:13
-- Verze serveru: 5.6.30-1~bpo8+1
-- Verze PHP: 5.6.39-0+deb8u1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Databáze: `db1_vyuka`
--

-- --------------------------------------------------------

--
-- Struktura tabulky `combo`
--

CREATE TABLE IF NOT EXISTS `combo` (
  `metric_name` varchar(50) NOT NULL,
  `object_name` varchar(50) NOT NULL,
  `algorithm_name` varchar(50) NOT NULL,
`id` int(11) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=262 DEFAULT CHARSET=utf8;

--
-- Vypisuji data pro tabulku `combo`
--

INSERT INTO `combo` (`metric_name`, `object_name`, `algorithm_name`, `id`) VALUES
('DameCL', 'fandisk', 'algo4', 219),
('DameCL', 'bimba', 'algo4', 220),
('DameCL', 'fandisk', 'algo3', 221),
('DameCL', 'maxplanck', 'algo4', 222),
('DameCL', 'maxplanck', 'algo3', 224),
('DameCL', 'fandisk', 'algo2', 225),
('DameCL', 'bimba', 'algo2', 226),
('DameCL', 'maxplanck', 'algo2', 227),
('DameCL', 'fandisk', 'algo1', 228),
('DameCL', 'maxplanck', 'algo1', 229),
('DameCL', 'welshdragon', 'algo4', 232),
('DameCL', 'bunny', 'algo4', 233),
('DameCL', 'welshdragon', 'algo3', 234),
('DameCL', 'f14470', 'algo3', 235),
('DameCL', 'bunny', 'algo3', 236),
('DameCL', 'bimba', 'algo3', 241),
('DameCL', 'bunny', 'algo1', 243),
('DameCL', 'chindragon', 'algo1', 244),
('DameCL', 'f14470', 'algo2', 245),
('DameCL', 'f14470', 'algo4', 247),
('DameCL', 'welshdragon', 'algo1', 248),
('DameCL', 'welshdragon', 'algo2', 249),
('DameCL', 'bunny', 'algo2', 250),
('DameCL', 'chindragon', 'algo2', 253),
('DameCL', 'bimba', 'algo1', 254),
('DameCL', 'chindragon', 'algo4', 256),
('DameCL', 'chindragon', 'algo3', 257),
('DameCL', 'Palmyra', 'algo3', 258),
('DameCL', 'Palmyra', 'algo4', 259),
('DameCL', 'Palmyra', 'algo1', 260),
('DameCL', 'Palmyra', 'algo2', 261);

--
-- Klíče pro exportované tabulky
--

--
-- Klíče pro tabulku `combo`
--
ALTER TABLE `combo`
 ADD PRIMARY KEY (`id`), ADD KEY `combo_algorithm_fk` (`algorithm_name`), ADD KEY `combo_metric_fk` (`metric_name`), ADD KEY `combo_object_fk` (`object_name`);

--
-- AUTO_INCREMENT pro tabulky
--

--
-- AUTO_INCREMENT pro tabulku `combo`
--
ALTER TABLE `combo`
MODIFY `id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=262;
--
-- Omezení pro exportované tabulky
--

--
-- Omezení pro tabulku `combo`
--
ALTER TABLE `combo`
ADD CONSTRAINT `combo_algorithm_fk` FOREIGN KEY (`algorithm_name`) REFERENCES `algorithm` (`name`),
ADD CONSTRAINT `combo_metric_fk` FOREIGN KEY (`metric_name`) REFERENCES `metric` (`name`),
ADD CONSTRAINT `combo_object_fk` FOREIGN KEY (`object_name`) REFERENCES `object` (`name`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
