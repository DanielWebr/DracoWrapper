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
-- Struktura tabulky `algorithm`
--

CREATE TABLE IF NOT EXISTS `algorithm` (
  `name` varchar(50) NOT NULL,
  `time` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Vypisuji data pro tabulku `algorithm`
--

INSERT INTO `algorithm` (`name`, `time`) VALUES
('algo1', '2019/05/15-17:26:45'),
('algo2', '2019/05/15-19:38:56'),
('algo3', '2019/05/15-19:48:02'),
('algo4', '2019/05/15-19:48:11');

--
-- Klíče pro exportované tabulky
--

--
-- Klíče pro tabulku `algorithm`
--
ALTER TABLE `algorithm`
 ADD PRIMARY KEY (`name`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
