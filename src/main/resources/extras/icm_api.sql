-- phpMyAdmin SQL Dump
-- version 4.9.0.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: 08. Nov, 2019 23:32 PM
-- Tjener-versjon: 10.4.6-MariaDB
-- PHP Version: 7.3.8

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `icm_api`
--
CREATE DATABASE IF NOT EXISTS `icm_api` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `icm_api`;

-- --------------------------------------------------------

--
-- Tabellstruktur for tabell `devices`
--

CREATE TABLE `devices` (
  `id` int(5) NOT NULL,
  `name` varchar(255) NOT NULL,
  `location` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dataark for tabell `devices`
--

INSERT INTO `devices` (`id`, `name`, `location`) VALUES
(1, 'arduino', 'home'),
(2, 'super', 'nasa');

-- --------------------------------------------------------

--
-- Tabellstruktur for tabell `measurements`
--

CREATE TABLE `measurements` (
  `id` int(11) NOT NULL,
  `type` varchar(255) NOT NULL,
  `current` float NOT NULL,
  `min` float NOT NULL,
  `max` float NOT NULL,
  `avg` float NOT NULL,
  `llm` float NOT NULL,
  `hlm` float NOT NULL,
  `mm_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dataark for tabell `measurements`
--

INSERT INTO `measurements` (`id`, `type`, `current`, `min`, `max`, `avg`, `llm`, `hlm`, `mm_id`) VALUES
(1, 'temperature', 18, 12, 30, 24, -10, 40, 1),
(3, 'co2', 1, 2, 3, 4, 5, 6, 1),
(4, 'temperature', 8, 5, 9, 6, 11, 8, 2),
(5, 'co2', 777777, 5554440, 8886750, 5567880000, 798866000, 8989900000, 2),
(6, 'humidity', 8, 8, 8, 8, 8, 8, 2);

-- --------------------------------------------------------

--
-- Tabellstruktur for tabell `measurement_meta`
--

CREATE TABLE `measurement_meta` (
  `id` int(5) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `device_id` int(5) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dataark for tabell `measurement_meta`
--

INSERT INTO `measurement_meta` (`id`, `timestamp`, `device_id`) VALUES
(1, '2019-11-08 09:00:00', 1),
(2, '2019-11-07 23:00:00', 2);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `devices`
--
ALTER TABLE `devices`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `measurements`
--
ALTER TABLE `measurements`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `measurement_meta`
--
ALTER TABLE `measurement_meta`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `devices`
--
ALTER TABLE `devices`
  MODIFY `id` int(5) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `measurements`
--
ALTER TABLE `measurements`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `measurement_meta`
--
ALTER TABLE `measurement_meta`
  MODIFY `id` int(5) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
